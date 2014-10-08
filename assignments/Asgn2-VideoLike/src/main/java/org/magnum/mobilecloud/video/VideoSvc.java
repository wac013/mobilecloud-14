/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.magnum.mobilecloud.video;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.magnum.mobilecloud.video.client.SecuredRestBuilder;
import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

import com.google.common.collect.Lists;

@Controller
public class VideoSvc {
	
	@Autowired
	private VideoRepository videos;

//	@GET(VIDEO_SVC_PATH)
//	public Collection<Video> getVideoList();
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		return Lists.newArrayList(videos.findAll());
	}

//	@GET(VIDEO_SVC_PATH + "/{id}")
//	public Video getVideoById(@Path("id") long id);
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}", method=RequestMethod.GET)
	public @ResponseBody Video getVideoById(@PathVariable("id") long id,
			HttpServletResponse response) {
		Video v = videos.findById(id);
		if(null == v)
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		else
			response.setStatus(HttpServletResponse.SC_OK);
		return v;
	}

//	@POST(VIDEO_SVC_PATH)
//	public Video addVideo(@Body Video v);
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v){
		 Video newvid = videos.save(v);
		 return newvid;
	}

//	@POST(VIDEO_SVC_PATH + "/{id}/like")
//	public Void likeVideo(@Path("id") long id);
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}/like", method=RequestMethod.POST)
//	public @ResponseBody void likeVideo(@PathVariable("id") long id,
//			Principal p, HttpServletResponse response) {
	public ResponseEntity<Void> likeVideo(@PathVariable("id") long id, Principal p) {
		Video v = videos.findById(id);
		if(null == v)
			//response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		else {
			String user = p.getName();
			List<String> likedBy = v.getLikedBy();
			if(!likedBy.contains(user)) {
				likedBy.add(user);
				v.setLikedBy(likedBy);
				v.setLikes(v.getLikes()+1);
				videos.save(v);
				//response.setStatus(HttpServletResponse.SC_OK);
				return new ResponseEntity<Void>(HttpStatus.OK);
			}
			else
				//response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
		}
	}
	
//	@POST(VIDEO_SVC_PATH + "/{id}/unlike")
//	public Void unlikeVideo(@Path("id") long id);
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}/unlike", method=RequestMethod.POST)
//	public @ResponseBody void unlikeVideo(@PathVariable("id") long id,
//			Principal p, HttpServletResponse response) {
	public ResponseEntity<Void> unlikeVideo(@PathVariable("id") long id, Principal p) {
		Video v = videos.findById(id);
		if(null == v)
//			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		else {
			String user = p.getName();
			List<String> likedBy = v.getLikedBy();
			if(likedBy.contains(user)) {
				likedBy.remove(user);
				v.setLikedBy(likedBy);
				v.setLikes(v.getLikes()-1);
				videos.save(v);
				//response.setStatus(HttpServletResponse.SC_OK);
				return new ResponseEntity<Void>(HttpStatus.OK);
			}
			else
				//response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
		}
	}

//	@GET(VIDEO_TITLE_SEARCH_PATH)
//	public Collection<Video> findByTitle(@Query(TITLE_PARAMETER) String title);
	@RequestMapping(value=VideoSvcApi.VIDEO_TITLE_SEARCH_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> findByTitle(
			@RequestParam(VideoSvcApi.TITLE_PARAMETER) String title) {
		return videos.findByName(title);
	}
	
//	@GET(VIDEO_DURATION_SEARCH_PATH)
//	public Collection<Video> findByDurationLessThan(@Query(DURATION_PARAMETER) long duration);
//	public @ResponseBody Collection<Video> findByDurationLessThan(
//			@RequestParam(VideoSvcApi.DURATION_PARAMETER) long duration) {
//		return videos.findByDurationLessThan(duration);
//	}
	
//	@GET(VIDEO_SVC_PATH + "/{id}/likedby")
//	public Collection<String> getUsersWhoLikedVideo(@Path("id") long id);
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}/likedby", method=RequestMethod.POST)
	public @ResponseBody Collection<String> getUsersWhoLikedVideo(@PathVariable("id") long id,
			HttpServletResponse response) {
//	public ResponseEntity<Collection<String>> getUsersWhoLikedVideo(@PathVariable("id") long id) {
		
		Video v = videos.findById(id);
		if(null == v)
		{
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		else {
			Collection<String> likedBy = v.likedBy();
			response.setStatus(HttpServletResponse.SC_OK);
			return likedBy;
		}
	}
	
}
