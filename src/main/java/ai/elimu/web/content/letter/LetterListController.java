package ai.elimu.web.content.letter;

import ai.elimu.dao.LetterDao;
import ai.elimu.model.content.Letter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/content/letter/list")
@RequiredArgsConstructor
public class LetterListController {

  private final Logger logger = LogManager.getLogger();

  private final LetterDao letterDao;

  @GetMapping
  public String handleRequest(Model model) {
    logger.info("handleRequest");

    List<Letter> letters = letterDao.readAllOrderedByUsage();
    model.addAttribute("letters", letters);

    int maxUsageCount = 0;
    for (Letter letter : letters) {
      if (letter.getUsageCount() > maxUsageCount) {
        maxUsageCount = letter.getUsageCount();
      }
    }
    model.addAttribute("maxUsageCount", maxUsageCount);

    return "content/letter/list";
  }
}
