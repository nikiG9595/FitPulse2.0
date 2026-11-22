package com.fitpulse.model.dto;
import jakarta.validation.constraints.*;
import com.fitpulse.service.GymContent;

public class GymSuggestionForm {
 @NotBlank(message = "{ui.enter.a.name}") @Size(max=150,message = "{ui.this.field.allows.up.to.max.characters}") private String name;
 @NotBlank(message = "{ui.enter.a.city}") @Size(max=100,message = "{ui.this.field.allows.up.to.max.characters}") private String cityName;
 @Size(max=300,message = "{ui.this.field.allows.up.to.max.characters}") private String address;
 @Size(max=500,message = "{ui.this.field.allows.up.to.max.characters}") private String websiteUrl;
 @Size(max=2000,message = "{ui.this.field.allows.up.to.max.characters}") private String details;
 @Size(max=50,message = "{ui.this.field.allows.up.to.max.characters}") private String phone;
 @Size(max=1000,message = "{ui.this.field.allows.up.to.max.characters}") private String priceInfo;
 @Size(max=1000,message = "{ui.this.field.allows.up.to.max.characters}") private String imageUrl;
 public String getName(){return name;} public void setName(String value){name=value;}
 public String getCityName(){return cityName;} public void setCityName(String value){cityName=value;}
 public String getAddress(){return address;} public void setAddress(String value){address=value;}
 public String getWebsiteUrl(){return websiteUrl;} public void setWebsiteUrl(String value){websiteUrl=value;}
 public String getDetails(){return details;} public void setDetails(String value){details=value;}
 public String getPhone(){return phone;} public void setPhone(String value){phone=value;}
 public String getPriceInfo(){return priceInfo;} public void setPriceInfo(String value){priceInfo=value;}
 public String getImageUrl(){return imageUrl;} public void setImageUrl(String value){imageUrl=value;}
 @AssertTrue(message = "{ui.enter.a.valid.http.or.https.website}") public boolean isWebsiteUrlValid(){return GymContent.webUrl(websiteUrl);}
 @AssertTrue(message = "{ui.enter.a.valid.http.https.or.local}") public boolean isImageUrlValid(){return GymContent.imageUrl(imageUrl);}
}

