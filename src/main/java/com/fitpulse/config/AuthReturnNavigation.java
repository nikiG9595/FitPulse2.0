package com.fitpulse.config;

import com.fitpulse.model.entity.*;
import com.fitpulse.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.http.server.PathContainer;
import java.net.URI;
import java.util.*;

/** Session-scoped destination, always revalidated after authentication. Never trusts Host/Referer as a redirect. */
@Component
public class AuthReturnNavigation implements AuthenticationSuccessHandler {
    private static final String KEY="FITPULSE_AUTH_RETURN";
    private final HttpSessionRequestCache cache=new HttpSessionRequestCache();
    private final ObjectProvider<RequestMappingHandlerMapping> mappings;
    private final UserService users;
    @PersistenceContext private EntityManager entities;
    public AuthReturnNavigation(@Qualifier("requestMappingHandlerMapping") ObjectProvider<RequestMappingHandlerMapping> mappings,UserService users){this.mappings=mappings;this.users=users;}

    public String internal(String value,HttpServletRequest request){
        if(value==null||value.length()>4096||value.chars().anyMatch(c->c<32||c==127)||value.contains("\\"))return null;
        try{
            URI uri=URI.create(value);String path=uri.getRawPath();
            if(uri.isAbsolute()||uri.getRawAuthority()!=null||path==null||!path.startsWith("/")||path.startsWith("//"))return null;
            String lower=path.toLowerCase(Locale.ROOT);
            if(lower.matches(".*%(?:2f|5c|2e|25|0[0-9a-f]|1[0-9a-f]|7f).*" )||path.contains(";")||path.contains("//")||!uri.normalize().getRawPath().equals(path))return null;
            String context=request.getContextPath();if(!context.isEmpty()&&(path.equals(context)||path.startsWith(context+"/")))path=path.substring(context.length());
            if(path.isEmpty())path="/";
            String decodedPath=URI.create(path).getPath();
            if(decodedPath.contains(";")||decodedPath.matches("/(?:login|register|logout|error)(?:/.*)?")||decodedPath.matches("/(?:api|css|js|images|vendor|actuator)(?:/.*)?")||decodedPath.endsWith("/data")||decodedPath.equals("/exercises/search"))return null;
            // Query and fragment remain intact. Encoded line breaks are never accepted in Location.
            if(value.toLowerCase(Locale.ROOT).matches(".*%(?:0a|0d|00).*"))return null;
            return URI.create(path+(uri.getRawQuery()==null?"":"?"+uri.getRawQuery())+(uri.getRawFragment()==null?"":"#"+uri.getRawFragment())).toASCIIString();
        }catch(IllegalArgumentException e){return null;}
    }
    public String pending(HttpServletRequest request){Object value=request.getSession().getAttribute(KEY);String safe=internal(value instanceof String?(String)value:null,request);return safe==null?"/":safe;}
    public void beginAuthentication(HttpServletRequest request,HttpServletResponse response)throws java.io.IOException{
        String original="GET".equals(request.getMethod())?internal(request.getRequestURI()+(request.getQueryString()==null?"":"?"+request.getQueryString()),request):null;
        response.sendRedirect(response.encodeRedirectURL(request.getContextPath()+"/login?returnTo="+java.net.URLEncoder.encode(original==null?"/":original,java.nio.charset.StandardCharsets.UTF_8)));
    }
    public void loginFailure(HttpServletRequest request,HttpServletResponse response)throws java.io.IOException{
        capture(request,response);
        response.sendRedirect(response.encodeRedirectURL(request.getContextPath()+"/login?error"));
    }
    public String capture(HttpServletRequest request,HttpServletResponse response){
        String explicit=request.getParameter("returnTo");String target=null;
        if(explicit!=null)target=internal(explicit,request);
        else if(request.getParameter("error")!=null||request.getParameter("registered")!=null)target=pending(request);
        // A direct /login or /register deliberately resets any stale destination to home.
        // Security's entry point supplies returnTo explicitly for intercepted protected GET pages.
        cache.removeRequest(request,response);
        request.getSession().setAttribute(KEY,target==null?"/":target);
        return pending(request);
    }
    private boolean getPage(String path){
        return mappings.getObject().getHandlerMethods().keySet().stream().anyMatch(info->{
            var methods=info.getMethodsCondition().getMethods();
            if(!methods.isEmpty()&&!methods.contains(RequestMethod.GET))return false;
            var patterns=info.getPathPatternsCondition();
            return patterns!=null&&patterns.getPatterns().stream().anyMatch(p->p.matches(PathContainer.parsePath(path)));
        });
    }
    private boolean own(Class<?> type,String id,User current){
        try{
            Object entity=entities.find(type,type==ProgressRecord.class?UUID.fromString(id):Long.valueOf(id));
            User owner=entity instanceof UserProgram p?p.getUser():entity instanceof UserMealPlan p?p.getUser():entity instanceof WorkoutLog p?p.getUser():entity instanceof WorkoutEvent p?p.getUser():entity instanceof WorkoutSession p?p.getUser():entity instanceof ProgressRecord p?p.getUser():entity instanceof BodyMeasurement p?p.getUser():null;
            return owner!=null&&owner.getId().equals(current.getId());
        }catch(IllegalArgumentException e){return false;}
    }
    @Transactional(readOnly=true)
    public String accessible(String target,Authentication authentication,HttpServletRequest request){
        String safe=internal(target,request);if(safe==null)return "/";
        URI uri=URI.create(safe);String path=uri.getPath();
        if(!getPage(path))return "/";
        boolean admin=authentication.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMIN"));
        if(path.startsWith("/admin")&&!admin)return "/";
        if(path.startsWith("/progress")&&!admin&&authentication.getAuthorities().stream().noneMatch(a->a.getAuthority().equals("ROLE_MEMBER")))return "/";
        User current=users.getCurrentUser();
        String[] parts=path.substring(1).split("/");
        if(Arrays.stream(parts).anyMatch(p->p.matches("[0-9]+")&&p.length()>18))return "/";
        if(parts.length>=3&&parts[0].equals("programs")&&parts[1].equals("my")&&!own(UserProgram.class,parts[2],current))return "/";
        if(parts.length>=3&&parts[0].equals("meal-plans")&&parts[1].equals("my")&&!own(UserMealPlan.class,parts[2],current))return "/";
        Map<String,Class<?>> personal=Map.of("journal",WorkoutLog.class,"calendar",WorkoutEvent.class,"training-sessions",WorkoutSession.class,"progress",ProgressRecord.class);
        if(parts.length>=2&&personal.containsKey(parts[0])&&!Set.of("create","data","measurements").contains(parts[1])&&!own(personal.get(parts[0]),parts[1],current))return "/";
        if(parts.length>=3&&parts[0].equals("progress")&&parts[1].equals("measurements")&&!own(BodyMeasurement.class,parts[2],current))return "/";
        if(parts.length==2&&!parts[1].equals("my")&&parts[0].equals("programs")){try{var p=entities.find(ProgramTemplate.class,Long.valueOf(parts[1]));if(p==null||!p.isActive())return "/";}catch(IllegalArgumentException e){return "/";}}
        if(parts.length>=2&&!parts[1].equals("my")&&parts[0].equals("meal-plans")){try{var p=entities.find(MealPlanTemplate.class,Long.valueOf(parts[1]));if(p==null||!p.isActive())return "/";}catch(IllegalArgumentException e){return "/";}}
        if(parts.length==2&&parts[0].equals("trainers")){try{var trainer=entities.find(Trainer.class,Long.valueOf(parts[1]));if(trainer==null||!trainer.isPublished())return "/";}catch(IllegalArgumentException e){return "/";}}
        if(parts.length==2&&parts[0].equals("gyms")&&entities.createQuery("select count(g) from Gym g where g.slug=:slug and g.status=:status",Long.class).setParameter("slug",parts[1]).setParameter("status",com.fitpulse.model.enums.ContentStatus.APPROVED).getSingleResult()==0)return "/";
        if(parts.length==2&&parts[0].equals("users")&&entities.createQuery("select count(u) from User u where u.username=:username and u.publicProfile=true",Long.class).setParameter("username",parts[1]).getSingleResult()==0)return "/";
        if(parts.length==2&&parts[0].equals("exercises")&&entities.createQuery("select count(e) from Exercise e where e.code=:code",Long.class).setParameter("code",parts[1]).getSingleResult()==0)return "/";
        if(uri.getRawQuery()!=null){
            var params=org.springframework.web.util.UriComponentsBuilder.fromUriString(safe).build().getQueryParams();
            for(var entry:Map.of("programId",UserProgram.class,"mealPlanId",UserMealPlan.class,"eventId",WorkoutEvent.class).entrySet())
                for(String id:params.getOrDefault(entry.getKey(),List.of()))if(!own(entry.getValue(),id,current))return "/";
        }
        return safe;
    }
    @Override @Transactional(readOnly=true)
    public void onAuthenticationSuccess(HttpServletRequest request,HttpServletResponse response,Authentication authentication)throws java.io.IOException{
        String explicit=internal(request.getParameter("returnTo"),request);
        String target=accessible(explicit==null?pending(request):explicit,authentication,request);
        cache.removeRequest(request,response);request.getSession().removeAttribute(KEY);request.getSession().removeAttribute("SPRING_SECURITY_LAST_EXCEPTION");
        response.sendRedirect(response.encodeRedirectURL(request.getContextPath()+target));
    }
}
