package com.fitpulse.service;
import com.fitpulse.exception.FitPulseException; import com.fitpulse.model.dto.*; import com.fitpulse.model.entity.*; import com.fitpulse.model.enums.ContentStatus; import com.fitpulse.repository.*; import org.springframework.data.domain.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.text.Normalizer; import java.util.*;
@Service public class GymDirectoryService {
 @org.springframework.beans.factory.annotation.Autowired private UserRepository userLocks;
 private final CityRepository cities; private final GymRepository gyms; private final GymPhotoRepository photos; private final ReviewRepository reviews; private final FavoriteGymRepository favorites; private final ReviewReportRepository reports; private final GymSuggestionRepository suggestions; private final UserService users;
 public GymDirectoryService(CityRepository c,GymRepository g,GymPhotoRepository p,ReviewRepository r,FavoriteGymRepository f,ReviewReportRepository rr,GymSuggestionRepository s,UserService u){cities=c;gyms=g;photos=p;reviews=r;favorites=f;reports=rr;suggestions=s;users=u;}
 public Page<Gym> search(String city,String q,int page){return gyms.search(ContentStatus.PUBLISHED,blank(city),blank(q),PageRequest.of(Math.max(0,page),12));}
 public List<City> activeCities(){return cities.findAllByActiveTrueOrderByName();}
 public Gym published(String slug){return gyms.findBySlugAndStatus(slug,ContentStatus.PUBLISHED).orElseThrow(()->new FitPulseException("Фитнесът не е намерен."));}
 public Gym byId(Long id){return gyms.findById(id).orElseThrow(()->new FitPulseException("Фитнесът не е намерен."));}
 public Gym publishedById(Long id){Gym gym=byId(id);if(gym.getStatus()!=ContentStatus.PUBLISHED)throw new FitPulseException("Фитнесът не е публикуван.");return gym;}
 public Review ownReview(Gym gym){return reviews.findByGymAndUser(gym,users.getCurrentUser()).orElse(null);}
 public List<Review> reviews(Gym gym){return reviews.findAllByGymAndStatusOrderByCreatedAtDesc(gym,ContentStatus.PUBLISHED);}
 public double rating(Gym gym){return reviews.averageForGym(gym);}
 public boolean favorite(Gym gym){return favorites.findByUserAndGym(users.getCurrentUser(),gym).isPresent();}
 @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED) public void toggleFavorite(Long gymId){User u=userLocks.lockForLibrary(users.getCurrentUser().getId()).orElseThrow();Gym g=publishedById(gymId);favorites.findByUserAndGym(u,g).ifPresentOrElse(favorites::delete,()->{FavoriteGym f=new FavoriteGym();f.setUser(u);f.setGym(g);favorites.save(f);});}
 public List<FavoriteGym> favorites(){return favorites.findAllByUserOrderByCreatedAtDesc(users.getCurrentUser()).stream().filter(f->f.getGym().getStatus()==ContentStatus.PUBLISHED).toList();}
 @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED) public Review review(Long gymId,ReviewForm form){
  if(form.getRating()<1||form.getRating()>5||GymContent.text(form.getComment()).length()>2000)throw new FitPulseException("Изберете оценка от 1 до 5 и ревю до 2000 знака.");
  User u=userLocks.lockForLibrary(users.getCurrentUser().getId()).orElseThrow();Gym g=publishedById(gymId);
  Review r=reviews.lockOwned(g,u).orElseGet(Review::new);r.setGym(g);r.setUser(u);r.setRating(form.getRating());r.setComment(GymContent.text(form.getComment()));
  if(r.getStatus()==ContentStatus.HIDDEN||r.getStatus()==ContentStatus.PENDING)r.setStatus(ContentStatus.PENDING);else r.setStatus(ContentStatus.PUBLISHED);
  return reviews.save(r);
 }
 @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED) public String report(Long reviewId,String reason){User u=userLocks.lockForLibrary(users.getCurrentUser().getId()).orElseThrow();Review r=reviews.findById(reviewId).orElseThrow(()->new FitPulseException("Ревюто не е намерено."));if(r.getStatus()!=ContentStatus.PUBLISHED||r.getGym().getStatus()!=ContentStatus.PUBLISHED)throw new FitPulseException("Ревюто не е достъпно.");String message=GymContent.text(reason);if(message.isBlank()||message.length()>500)throw new FitPulseException("Въведете причина до 500 знака.");if(reports.existsByReviewAndReporter(r,u))throw new FitPulseException("Вече сте докладвали това ревю.");ReviewReport x=new ReviewReport();x.setReview(r);x.setReporter(u);x.setReason(message);reports.save(x);return r.getGym().getSlug();}
 @Transactional public void suggest(GymSuggestionForm f){GymSuggestion s=new GymSuggestion();s.setSubmittedBy(users.getCurrentUser());s.setName(GymContent.text(f.getName()));s.setCityName(GymContent.text(f.getCityName()));s.setAddress(GymContent.text(f.getAddress()));s.setWebsiteUrl(GymContent.text(f.getWebsiteUrl()));s.setDetails(GymContent.text(f.getDetails()));s.setPhone(GymContent.text(f.getPhone()));s.setPriceInfo(GymContent.text(f.getPriceInfo()));s.setImageUrl(GymContent.text(f.getImageUrl()));s.setStatus(ContentStatus.PENDING);suggestions.save(s);}
 public List<Gym> allGyms(){return gyms.findAll(Sort.by("name"));} public List<City> allCities(){return cities.findAll(Sort.by("name"));} public List<GymSuggestion> allSuggestions(){return suggestions.findAllByOrderByCreatedAtDesc();} public List<Review> allReviews(){return reviews.findAll(Sort.by(Sort.Direction.DESC,"createdAt"));} public List<ReviewReport> allReports(){return reports.findAll(Sort.by(Sort.Direction.DESC,"createdAt"));}
 @Transactional public Gym saveGym(Long id,GymForm f){if(!GymContent.webUrl(f.getWebsiteUrl())||!Set.of(ContentStatus.DRAFT,ContentStatus.PUBLISHED,ContentStatus.ARCHIVED).contains(f.getStatus()))throw new FitPulseException("Проверете уебсайта и статуса на фитнеса.");Gym g=id==null?new Gym():byId(id);g.setCity(cities.findById(f.getCityId()).orElseThrow(()->new FitPulseException("Градът не е намерен.")));g.setName(f.getName());if(id==null||g.getSlug()==null||g.getSlug().isBlank())g.setSlug(uniqueSlug(f.getName(),id));g.setDescription(GymContent.text(f.getDescription()));g.setAddress(GymContent.text(f.getAddress()));g.setLatitude(f.getLatitude());g.setLongitude(f.getLongitude());g.setPhone(f.getPhone());g.setWebsiteUrl(GymContent.text(f.getWebsiteUrl()));g.setOpeningHours(f.getOpeningHours());g.setServices(f.getServices());g.setPriceInfo(f.getPriceInfo());g.setStatus(f.getStatus());return gyms.save(g);}
 @Transactional public void deleteGym(Long id){byId(id).setStatus(ContentStatus.ARCHIVED);}
 public GymForm form(Gym g){GymForm f=new GymForm();f.setCityId(g.getCity().getId());f.setName(g.getName());f.setDescription(g.getDescription());f.setAddress(g.getAddress());f.setLatitude(g.getLatitude());f.setLongitude(g.getLongitude());f.setPhone(g.getPhone());f.setWebsiteUrl(g.getWebsiteUrl());f.setOpeningHours(g.getOpeningHours());f.setServices(g.getServices());f.setPriceInfo(g.getPriceInfo());f.setStatus(g.getStatus());return f;}
 @Transactional public void addPhoto(Long gymId,String url,String alt){String image=GymContent.text(url),description=GymContent.text(alt);if(image.isBlank()||image.length()>1000||!GymContent.imageUrl(image)||description.length()>250)throw new FitPulseException("Въведете валиден адрес на снимка до 1000 знака и описание до 250 знака.");Gym gym=byId(gymId);if(gym.getPhotos().stream().anyMatch(p->p.getImageUrl().equals(image)))return;GymPhoto p=new GymPhoto();p.setGym(gym);p.setImageUrl(image);p.setAltText(description.isBlank()?gym.getName():description);p.setSortOrder(gym.getPhotos().size());photos.save(p);}
 @Transactional public Long deletePhoto(Long photoId){GymPhoto p=photos.findById(photoId).orElseThrow(()->new FitPulseException("Снимката не е намерена."));Long gymId=p.getGym().getId();photos.delete(p);return gymId;}
 @Transactional public City saveCity(Long id,String name){City c=id==null?new City():cities.findById(id).orElseThrow(()->new FitPulseException("Градът не е намерен."));c.setName(name);c.setSlug(slug(name));return cities.save(c);}
 @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED) public void suggestionStatus(Long id,ContentStatus status,String note){
  if(status!=ContentStatus.APPROVED&&status!=ContentStatus.REJECTED)throw new FitPulseException("Изберете одобрение или отхвърляне.");
  if(GymContent.text(note).length()>1000)throw new FitPulseException("Бележката допуска до 1000 знака.");
  GymSuggestion s=suggestions.lockById(id).orElseThrow(()->new FitPulseException("Предложението не е намерено."));
  if(status==ContentStatus.REJECTED&&s.getPublishedGym()!=null)throw new FitPulseException("Предложението вече е свързано със зала. За премахване от каталога използвайте архивирането на самата зала.");
  if(status==ContentStatus.APPROVED){
   if(!GymContent.webUrl(s.getWebsiteUrl())||!GymContent.imageUrl(s.getImageUrl()))throw new FitPulseException("Предложението съдържа невалиден адрес на сайт или снимка.");
   Gym gym=s.getPublishedGym();
   if(gym==null){
    String cityName=GymContent.text(s.getCityName());
    City city=cities.findAll().stream().filter(c->c.getName().equalsIgnoreCase(cityName)).findFirst().orElse(null);
    if(city==null){city=new City();city.setName(cityName);String base=slug(cityName),candidate=base;int index=2;while(cities.findBySlug(candidate).isPresent())candidate=base+"-"+index++;city.setSlug(candidate);city=cities.save(city);}
    city=cities.lockById(city.getId()).orElseThrow();
    final Long cityId=city.getId();String address=GymContent.text(s.getAddress());
    gym=gyms.findAll().stream().filter(g->!address.isBlank()&&g.getCity().getId().equals(cityId)&&g.getName().equalsIgnoreCase(s.getName())&&g.getAddress().equalsIgnoreCase(address)).findFirst().orElse(null);
    if(gym==null){gym=new Gym();gym.setCity(city);gym.setName(s.getName());gym.setSlug(uniqueSlug(s.getName(),null));gym.setAddress(address);gym.setDescription(GymContent.text(s.getDetails()));gym.setWebsiteUrl(GymContent.text(s.getWebsiteUrl()));gym.setPhone(GymContent.text(s.getPhone()));gym.setPriceInfo(GymContent.text(s.getPriceInfo()));gym=gyms.save(gym);}
    s.setPublishedGym(gym);
    if(s.getImageUrl()!=null&&!s.getImageUrl().isBlank())addPhoto(gym.getId(),s.getImageUrl(),gym.getName());
   }
   gym.setStatus(ContentStatus.PUBLISHED);
  }
  s.setStatus(status);s.setAdminNote(GymContent.text(note));
 }
 @Transactional public void reviewStatus(Long id,ContentStatus status){if(!Set.of(ContentStatus.PUBLISHED,ContentStatus.HIDDEN,ContentStatus.PENDING).contains(status))throw new FitPulseException("Невалиден статус на ревю.");Review r=reviews.lockById(id).orElseThrow(()->new FitPulseException("Ревюто не е намерено."));r.setStatus(status);}
 @Transactional public void resolveReport(Long id){reports.findById(id).orElseThrow(()->new FitPulseException("Сигналът не е намерен.")).setStatus(ContentStatus.RESOLVED);}
 public long publishedCount(){return gyms.countByStatus(ContentStatus.PUBLISHED);} public long pendingCount(){return suggestions.countByStatus(ContentStatus.PENDING);} public long reportCount(){return reports.countByStatus(ContentStatus.OPEN);}
 private String uniqueSlug(String name,Long id){String base=slug(name),candidate=base;int i=2;while(gyms.findBySlug(candidate).filter(g->!Objects.equals(g.getId(),id)).isPresent())candidate=base+"-"+i++;return candidate;}
 private String slug(String s){String n=Normalizer.normalize(s,Normalizer.Form.NFD).replaceAll("\\p{M}","").toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+","-").replaceAll("(^-|-$)","");return n.isBlank()?"item-"+System.currentTimeMillis():n;}
 private String blank(String s){return s==null||s.isBlank()?null:s.trim();}
}
