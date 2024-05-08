package org.example.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;


public class UserBean {

    @JsonFormat
    private String userId;
    @JsonFormat
    private String avatar;

    private boolean isPhone;

   


    public UserBean(String userId, String avatar) {
        this.userId = userId;
        this.avatar = avatar;
    }

   
    


    @JsonIgnore
    public String getUserId() {
        return userId;
    }

    @JsonIgnore
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @JsonIgnore
    public String getAvatar() {
        return avatar;
    }

    @JsonIgnore
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (null == obj) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        UserBean user = (UserBean) obj;
        return this.userId.equals(user.getUserId());
    }

    public boolean isPhone() {
        return isPhone;
    }

    public void setPhone(boolean phone) {
        isPhone = phone;
    }
}
