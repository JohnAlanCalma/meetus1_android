package com.example.yun.meetup.managers;

import android.text.Html;

import com.example.yun.meetup.models.APIResult;
import com.example.yun.meetup.models.Event;
import com.example.yun.meetup.models.UserInfo;
import com.example.yun.meetup.providers.ApiProvider;
import com.example.yun.meetup.requests.CreateEventRequest;
import com.example.yun.meetup.requests.EventListRequest;
import com.example.yun.meetup.requests.LoginRequest;
import com.example.yun.meetup.requests.RegistrationRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class NetworkManager {

    private static final String GOOGLE_API_GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json";
    private static final String GOOGLE_API_KEY = "AIzaSyAcujkeJzWDW0S31u_tCf2o9B3K0e15Z-U";
    private static ApiProvider apiProvider = new ApiProvider();

    public APIResult login(LoginRequest loginRequest) {

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String json = gson .toJson(loginRequest);

        APIResult apiResult = new APIResult(false, "Login failed: please try again", null);

        try{

            String response = apiProvider.sendRequest("/user/login", "POST", json);

            JSONObject responseJSON = new JSONObject(response);
            if (!responseJSON.isNull("data")) {

                UserInfo userInfo = gson.fromJson(responseJSON.getJSONObject("data").toString(), UserInfo.class);

                apiResult = new APIResult(true, APIResult.RESULT_SUCCESS, userInfo);
            }
            else if (!responseJSON.isNull("err") && responseJSON.getString("err").equals("User not found")){
                apiResult = new APIResult(false, "Wrong email/password. Please try again", null);
            }
        }
        catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        return apiResult;
    }

    public APIResult register(RegistrationRequest registrationRequest) {

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String json = gson .toJson(registrationRequest);

        APIResult apiResult = new APIResult(false, "Registration failed: please try again", null);

        try {

            String response = apiProvider.sendRequest("/user/register", "POST", json);

            JSONObject responseJSON = new JSONObject(response);

            if (!responseJSON.isNull("data")) {

                UserInfo userInfo = gson.fromJson(responseJSON.getJSONObject("data").toString(), UserInfo.class);

                apiResult = new APIResult(true, APIResult.RESULT_SUCCESS, userInfo);

            }
            else if (!responseJSON.isNull("err") && responseJSON.getString("err").equals("Email is already used")){
                apiResult = new APIResult(false, "Email is already used: please try again", null);
            }
        }
        catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        return apiResult;
    }

    public APIResult validateEventAddress(CreateEventRequest createEventRequest) {

        APIResult apiResult = new APIResult(false, "Failed converting address to location: please try again", null);

        try {
            String response = apiProvider.sendRequest(GOOGLE_API_GEOCODE_URL + "?key=" + GOOGLE_API_KEY + "&address=" + URLEncoder.encode(createEventRequest.getAddress(), "UTF-8"), "GET", null);

            JSONObject responseJSON = new JSONObject(response);

            if (!responseJSON.isNull("status")) {

                if (responseJSON.getString("status").equals("OK")) {
                    JSONObject result = responseJSON.getJSONArray("results").getJSONObject(0);
                    createEventRequest.setAddress(result.getString("formatted_address"));
                    createEventRequest.setLatitude(Float.parseFloat(result.getJSONObject("geometry").getJSONObject("location").getString("lat")));
                    createEventRequest.setLongitude(Float.parseFloat(result.getJSONObject("geometry").getJSONObject("location").getString("lng")));
                    apiResult = new APIResult(true, APIResult.RESULT_SUCCESS, createEventRequest);
                }
                else if (responseJSON.getString("status").equals("ZERO_RESULTS")) {
                    apiResult = new APIResult(false, "No address location found. Please enter a valid address!", null);
                }

            }
        }
        catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        return apiResult;
    }

    public APIResult createEvent(CreateEventRequest createEventRequest) {

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String json = gson .toJson(createEventRequest);

        APIResult apiResult = new APIResult(false, "Failed creating the event: please try again", null);

        try {

            String response = apiProvider.sendRequest("/event", "POST", json);

            JSONObject responseJSON = new JSONObject(response);

            if (!responseJSON.isNull("data")) {

                Event event = gson.fromJson(responseJSON.getJSONObject("data").toString(), Event.class);

                apiResult = new APIResult(true, APIResult.RESULT_SUCCESS, event);

            }
        }
        catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        return apiResult;
    }

    public APIResult getHostedEvents(EventListRequest eventListRequest){

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String json = gson .toJson(eventListRequest);

        APIResult apiResult = new APIResult(false, "Failed getting list of hosted events: please try again", null);

        try {

            String response = apiProvider.sendRequest("/host_event", "POST", json);

            JSONObject responseJSON = new JSONObject(response);

            if (!responseJSON.isNull("data")) {

                Event[] arrayEvent = gson.fromJson(responseJSON.getJSONArray("data").toString(), Event[].class);

                List<Event> listEvents = new ArrayList<>();

                for (Event event : arrayEvent){
                    listEvents.add(event);
                }

                apiResult = new APIResult(true, APIResult.RESULT_SUCCESS, listEvents);

            }
        }
        catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        return apiResult;
    }


}
