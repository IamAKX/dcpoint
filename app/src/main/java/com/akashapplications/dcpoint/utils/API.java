package com.akashapplications.dcpoint.utils;

public class API {
    public static final String BASE_URL = "https://us-central1-dcpoint.cloudfunctions.net/";

    public static final String REGISTER = BASE_URL + "userFunction/v1/user/register";
    public static final String LOGIN = BASE_URL + "userFunction/v1/user/login";
    public static final String DELETE_USER = BASE_URL + "userFunction/v1/user/register";


    public static final String ADD_RECORD = BASE_URL + "recordFunction/v1/record/add";
    public static final String VIEW_RECORD = BASE_URL + "recordFunction/v1/record/view";
    public static final String VIEW_ALL_RECORD = BASE_URL + "recordFunction/v1/record/viewAll";
    public static final String DELETE_RECORD = BASE_URL + "recordFunction/v1/record/delete";
    public static final String QUERY_RECORD = BASE_URL + "recordFunction/v1/record/query";
}
