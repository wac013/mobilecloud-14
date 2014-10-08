package org.magnum.dataup;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;


@Controller
public class VideoSvc {

	private Map<Long,Video> videos = new HashMap<Long, Video>();
	private static final AtomicLong currentId = new AtomicLong(0L);
	
	private VideoFileManager videoDataMgr;
	
	public VideoSvc() {
		try {
			videoDataMgr = VideoFileManager.get();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		return videos.values();
	}

	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v) {
		save(v).
			setDataUrl(getDataUrl(v.getId()));
		return v;
	}

	@RequestMapping(value="/video/{id}/data", method=RequestMethod.POST)
	public @ResponseBody VideoStatus setVideoData(@PathVariable(VideoSvcApi.ID_PARAMETER) long id,
									@RequestParam(VideoSvcApi.DATA_PARAMETER) MultipartFile videoData,
									HttpServletResponse response) {
		Video v = videos.get(id);
		if(null == v)
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		else
		{
			try {
				saveSomeVideo(v, videoData);
				response.setStatus(HttpServletResponse.SC_OK);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return new VideoStatus(VideoStatus.VideoState.READY);
	}

	@RequestMapping(value="/video/{id}/data", method=RequestMethod.GET)
	public @ResponseBody void getData(@PathVariable(VideoSvcApi.ID_PARAMETER) long id,
									HttpServletResponse response) {
		Video v = videos.get(id);
		if(null == v)
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		else
		{
			try {
				serveSomeVideo(v, response);
				response.setStatus(HttpServletResponse.SC_OK);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

    private String getDataUrl(long videoId){
        String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
        return url;
    }

 	private String getUrlBaseForLocalServer() {
	   HttpServletRequest request = 
	       ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	   String base = 
	      "http://"+request.getServerName() 
	      + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
	   return base;
	}
 	
  	private Video save(Video entity) {
		checkAndSetId(entity);
		videos.put(entity.getId(), entity);
		return entity;
	}

	private void checkAndSetId(Video entity) {
		if (entity.getId() == 0) {
			entity.setId(currentId.incrementAndGet());
		}
	}
	
  	private void saveSomeVideo(Video v, MultipartFile videoData) throws IOException {
  		videoDataMgr.saveVideoData(v, videoData.getInputStream());
 	}
  	
  	private void serveSomeVideo(Video v, HttpServletResponse response) throws IOException {
  		videoDataMgr.copyVideoData(v, response.getOutputStream());
 	}
}
