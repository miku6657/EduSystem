package com.keshe.edumanage;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


public class BCryptTest {


    public static void main(String[] args) {


        BCryptPasswordEncoder encoder =
                new BCryptPasswordEncoder();


        String password = "123456";


        String encode =
                encoder.encode(password);


        System.out.println(encode);

    }

}