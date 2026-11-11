package com.fitpulse.model.dto;
import jakarta.validation.constraints.*;
import com.fitpulse.service.GymContent;

public class GymSuggestionForm {
 @NotBlank(message="Въведете име.") @Size(max=150,message="Полето допуска най-много {max} знака.") private String name;
 @NotBlank(message="Въведете град.") @Size(max=100,message="Полето допуска най-много {max} знака.") private String cityName;
 @Size(max=300,message="Полето допуска най-много {max} знака.") private String address;
 @Size(max=500,message="Полето допуска най-много {max} знака.") private String websiteUrl;
 @Size(max=2000,message="Полето допуска най-много {max} знака.") private String details;
 @Size(max=50,message="Полето допуска най-много {max} знака.") private String phone;
 @Size(max=1000,message="Полето допуска най-много {max} знака.") private String priceInfo;
 @Size(max=1000,message="Полето допуска най-много {max} знака.") private String imageUrl;
 public String getName(){return name;} public void setName(String value){name=value;}
 public String getCityName(){return cityName;} public void setCityName(String value){cityName=value;}
 public String getAddress(){return address;} public void setAddress(String value){address=value;}
 public String getWebsiteUrl(){return websiteUrl;} public void setWebsiteUrl(String value){websiteUrl=value;}
 public String getDetails(){return details;} public void setDetails(String value){details=value;}
 public String getPhone(){return phone;} public void setPhone(String value){phone=value;}
 public String getPriceInfo(){return priceInfo;} public void setPriceInfo(String value){priceInfo=value;}
 public String getImageUrl(){return imageUrl;} public void setImageUrl(String value){imageUrl=value;}
 @AssertTrue(message="Уебсайтът трябва да бъде валиден HTTP/HTTPS адрес.") public boolean isWebsiteUrlValid(){return GymContent.webUrl(websiteUrl);}
 @AssertTrue(message="Снимката трябва да има валиден HTTP/HTTPS или локален адрес.") public boolean isImageUrlValid(){return GymContent.imageUrl(imageUrl);}
}

