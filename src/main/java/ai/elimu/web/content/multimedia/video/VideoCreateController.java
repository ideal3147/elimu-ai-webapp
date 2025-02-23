package ai.elimu.web.content.multimedia.video;

import ai.elimu.dao.VideoDao;
import ai.elimu.model.content.multimedia.Video;
import ai.elimu.model.enums.ContentLicense;
import ai.elimu.model.v2.enums.content.LiteracySkill;
import ai.elimu.model.v2.enums.content.NumeracySkill;
import ai.elimu.model.v2.enums.content.VideoFormat;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Calendar;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;

@Controller
@RequestMapping("/content/multimedia/video/create")
@RequiredArgsConstructor
@Slf4j
public class VideoCreateController {

  private final VideoDao videoDao;

  @RequestMapping(method = RequestMethod.GET)
  public String handleRequest(Model model) {
    log.info("handleRequest");

    Video video = new Video();
    model.addAttribute("video", video);

    model.addAttribute("contentLicenses", ContentLicense.values());

    model.addAttribute("literacySkills", LiteracySkill.values());
    model.addAttribute("numeracySkills", NumeracySkill.values());

    return "content/multimedia/video/create";
  }

  @RequestMapping(method = RequestMethod.POST)
  public String handleSubmit(
      /*@Valid*/ Video video,
      @RequestParam("bytes") MultipartFile multipartFile,
      @RequestParam("thumbnail") MultipartFile multipartFileThumbnail,
      BindingResult result,
      Model model) {
    log.info("handleSubmit");

    if (StringUtils.isBlank(video.getTitle())) {
      result.rejectValue("title", "NotNull");
    } else {
      Video existingVideo = videoDao.read(video.getTitle());
      if (existingVideo != null) {
        result.rejectValue("title", "NonUnique");
      }
    }

    try {
      byte[] bytes = multipartFile.getBytes();
      if (multipartFile.isEmpty() || (bytes == null) || (bytes.length == 0)) {
        result.rejectValue("bytes", "NotNull");
      } else {
        String originalFileName = multipartFile.getOriginalFilename();
        log.info("originalFileName: " + originalFileName);
        if (originalFileName.toLowerCase().endsWith(".m4v")) {
          video.setVideoFormat(VideoFormat.M4V);
        } else if (originalFileName.toLowerCase().endsWith(".mp4")) {
          video.setVideoFormat(VideoFormat.MP4);
        } else {
          result.rejectValue("bytes", "typeMismatch");
        }

        if (video.getVideoFormat() != null) {
          String contentType = multipartFile.getContentType();
          log.info("contentType: " + contentType);
          video.setContentType(contentType);

          video.setBytes(bytes);

          // TODO: convert to a default video format?
        }
      }

      byte[] bytesThumbnail = multipartFileThumbnail.getBytes();
      if (multipartFileThumbnail.isEmpty() || (bytesThumbnail == null) || (bytesThumbnail.length == 0)) {
        result.rejectValue("thumbnail", "NotNull");
      } else {
        String originalFileName = multipartFileThumbnail.getOriginalFilename();
        log.info("originalFileName: " + originalFileName);
        if (!originalFileName.toLowerCase().endsWith(".png")) {
          result.rejectValue("thumbnail", "typeMismatch");
        } else {
          video.setThumbnail(bytesThumbnail);
        }
      }
    } catch (IOException e) {
      log.error(e.getMessage());
    }

    if (result.hasErrors()) {
      model.addAttribute("contentLicenses", ContentLicense.values());

      model.addAttribute("literacySkills", LiteracySkill.values());
      model.addAttribute("numeracySkills", NumeracySkill.values());

      return "content/multimedia/video/create";
    } else {
      video.setTitle(video.getTitle().toLowerCase());
      video.setTimeLastUpdate(Calendar.getInstance());
      videoDao.create(video);

      return "redirect:/content/multimedia/video/list#" + video.getId();
    }
  }

  /**
   * See http://www.mkyong.com/spring-mvc/spring-mvc-failed-to-convert-property-value-in-file-upload-form/
   * <p></p>
   * Fixes this error message: "Cannot convert value of type [org.springframework.web.multipart.support.StandardMultipartHttpServletRequest$StandardMultipartFile] to required type [byte] for property
   * 'bytes[0]'"
   */
  @InitBinder
  protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws ServletException {
    log.info("initBinder");
    binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
  }
}
