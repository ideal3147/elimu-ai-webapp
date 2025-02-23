package ai.elimu.web.download;

import ai.elimu.dao.ApplicationDao;
import ai.elimu.dao.ApplicationVersionDao;
import ai.elimu.model.admin.Application;
import ai.elimu.model.admin.ApplicationVersion;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/apk")
@RequiredArgsConstructor
@Slf4j
public class ApkController {

  private final ApplicationDao applicationDao;

  private final ApplicationVersionDao applicationVersionDao;

  @RequestMapping(value = "/{packageName}-{versionCode}.apk", method = RequestMethod.GET)
  public void handleRequest(
      @PathVariable String packageName,
      @PathVariable Integer versionCode,

      HttpServletRequest request,
      HttpServletResponse response,
      OutputStream outputStream) {
    log.info("handleRequest");

    log.info("packageName: " + packageName);
    log.info("versionCode: " + versionCode);
    log.info("request.getQueryString(): " + request.getQueryString());
    log.info("request.getRemoteAddr(): " + request.getRemoteAddr());

    Application application = applicationDao.readByPackageName(packageName);
    log.info("application: " + application);

    ApplicationVersion applicationVersion = applicationVersionDao.read(application, versionCode);
    log.info("applicationVersion: " + applicationVersion);

    response.setContentType(applicationVersion.getContentType());

    byte[] bytes = applicationVersion.getBytes();
    response.setContentLength(bytes.length);
    try {
      outputStream.write(bytes);
    } catch (EOFException ex) {
      // org.eclipse.jetty.io.EofException (occurs when download is aborted before completion)
      log.warn(ex.getMessage());
    } catch (IOException ex) {
      log.error(ex.getMessage());
    } finally {
      try {
        try {
          outputStream.flush();
          outputStream.close();
        } catch (EOFException ex) {
          // org.eclipse.jetty.io.EofException (occurs when download is aborted before completion)
          log.warn(ex.getMessage());
        }
      } catch (IOException ex) {
        log.error(ex.getMessage());
      }
    }
  }
}
