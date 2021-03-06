/**
 * Copyright (C) 2013 - 2015 the enviroCar community
 *
 * This file is part of the enviroCar app.
 *
 * The enviroCar app is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The enviroCar app is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with the enviroCar app. If not, see http://www.gnu.org/licenses/.
 */
package org.envirocar.remote.service;

import com.squareup.okhttp.ResponseBody;


import org.envirocar.core.entity.User;
import org.envirocar.core.entity.UserStatistics;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import rx.Observable;

/**
 * @author dewall
 */
public interface UserService {

    @GET("users/{user}")
    Call<User> getUser(@Path("user") String user);

    @GET("users/{user}")
    Observable<User> getUserObservable(@Path("user") String user);

    @POST("users/")
    Call<ResponseBody> createUser(@Body User user);

    @PUT("users/{user}")
    Call<ResponseBody> updateUser(@Path("user") String user, @Body User body);

    @GET("users/{user}/statistics")
    Call<UserStatistics> getUserStatistics(@Path("user") String user);

    @GET("users/{user}/statistics")
    Observable<UserStatistics> getUserStatisticsObservable(@Path("user") String user);
}
