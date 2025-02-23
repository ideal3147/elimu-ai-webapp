package ai.elimu.web.content.emoji;

import ai.elimu.dao.EmojiDao;
import ai.elimu.model.content.Emoji;
import jakarta.validation.Valid;
import java.util.Calendar;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/content/emoji/create")
@RequiredArgsConstructor
@Slf4j
public class EmojiCreateController {

  private final EmojiDao emojiDao;

  @RequestMapping(method = RequestMethod.GET)
  public String handleRequest(Model model) {
    log.info("handleRequest");

    Emoji emoji = new Emoji();
    model.addAttribute("emoji", emoji);

    return "content/emoji/create";
  }

  @RequestMapping(method = RequestMethod.POST)
  public String handleSubmit(
      @Valid Emoji emoji,
      BindingResult result,
      Model model) {
    log.info("handleSubmit");

    Emoji existingEmoji = emojiDao.readByGlyph(emoji.getGlyph());
    if (existingEmoji != null) {
      result.rejectValue("glyph", "NonUnique");
    }

    if (emoji.getUnicodeVersion() > 9) {
      result.rejectValue("glyph", "emoji.unicode.version");
    }

    if (result.hasErrors()) {
      model.addAttribute("emoji", emoji);

      return "content/emoji/create";
    } else {
      emoji.setTimeLastUpdate(Calendar.getInstance());
      emojiDao.create(emoji);

      return "redirect:/content/emoji/list#" + emoji.getId();
    }
  }
}
