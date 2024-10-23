package com.hmdp.utils;

import com.hmdp.entity.User;
import com.hmdp.dto.UserDTO;

public class UserHolder {
    //ThreadLocal 为每个线程提供一个独立的变量副本。这样在多线程环境中，每个线程都可以独立存取其自己的数据，而不会互相影响。
    // 在这里，UserHolder 中的 ThreadLocal<UserDTO> tl 用于存储当前请求的用户信息。
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    public static void saveUser(UserDTO user){
        tl.set(user);
    }

    public static UserDTO getUser(){
        return tl.get();
    }

    public static void removeUser(){
        tl.remove();
    }
}
