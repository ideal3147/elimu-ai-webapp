package ai.elimu.web.content.letter_sound;

import ai.elimu.dao.LetterSoundDao;
import ai.elimu.model.content.LetterSound;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/content/letter-sound/list")
@RequiredArgsConstructor
@Slf4j
public class LetterSoundListController {

  private final LetterSoundDao letterSoundDao;

  @RequestMapping(method = RequestMethod.GET)
  public String handleRequest(Model model) {
    log.info("handleRequest");

    List<LetterSound> letterSounds = letterSoundDao.readAllOrderedByUsage();
    model.addAttribute("letterSounds", letterSounds);

    int maxUsageCount = 0;
    for (LetterSound letterSound : letterSounds) {
      if (letterSound.getUsageCount() > maxUsageCount) {
        maxUsageCount = letterSound.getUsageCount();
      }
    }
    model.addAttribute("maxUsageCount", maxUsageCount);

    return "content/letter-sound/list";
  }
}
