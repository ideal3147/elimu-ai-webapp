package ai.elimu.web.content.letter_sound;

import ai.elimu.dao.LetterSoundDao;
import ai.elimu.model.content.Letter;
import ai.elimu.model.content.LetterSound;
import ai.elimu.model.content.Sound;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/content/letter-sound/list")
@RequiredArgsConstructor
public class LetterSoundCsvExportController {

  private final Logger logger = LogManager.getLogger();

  private final LetterSoundDao letterSoundDao;

  @GetMapping(value = "/letter-sounds.csv")
  public void handleRequest(
      HttpServletResponse response,
      OutputStream outputStream
  ) throws IOException {
    logger.info("handleRequest");

    List<LetterSound> letterSounds = letterSoundDao.readAllOrderedById();
    logger.info("letterSounds.size(): " + letterSounds.size());

    CSVFormat csvFormat = CSVFormat.DEFAULT
        .withHeader(
            "id",
            "letter_ids",
            "letter_texts",
            "sound_ids",
            "sound_values_ipa",
            "usage_count"
        );
    StringWriter stringWriter = new StringWriter();
    CSVPrinter csvPrinter = new CSVPrinter(stringWriter, csvFormat);

    for (LetterSound letterSound : letterSounds) {
      logger.info("letterSound.getId(): \"" + letterSound.getId() + "\"");

      JSONArray letterIdsJsonArray = new JSONArray();
      int index = 0;
      for (Letter letter : letterSound.getLetters()) {
        letterIdsJsonArray.put(index, letter.getId());
        index++;
      }

      JSONArray letterTextsJsonArray = new JSONArray();
      index = 0;
      for (Letter letter : letterSound.getLetters()) {
        letterTextsJsonArray.put(index, letter.getText());
        index++;
      }

      JSONArray soundIdsJsonArray = new JSONArray();
      index = 0;
      for (Sound sound : letterSound.getSounds()) {
        soundIdsJsonArray.put(index, sound.getId());
        index++;
      }

      JSONArray soundValuesIpaJsonArray = new JSONArray();
      index = 0;
      for (Sound sound : letterSound.getSounds()) {
        soundValuesIpaJsonArray.put(index, sound.getValueIpa());
        index++;
      }

      csvPrinter.printRecord(letterSound.getId(),
          letterIdsJsonArray,
          letterTextsJsonArray,
          soundIdsJsonArray,
          soundValuesIpaJsonArray,
          letterSound.getUsageCount()
      );

      csvPrinter.flush();
    }
    csvPrinter.close();

    String csvFileContent = stringWriter.toString();

    response.setContentType("text/csv");
    byte[] bytes = csvFileContent.getBytes();
    response.setContentLength(bytes.length);
    try {
      outputStream.write(bytes);
      outputStream.flush();
      outputStream.close();
    } catch (IOException ex) {
      logger.error(ex);
    }
  }
}
