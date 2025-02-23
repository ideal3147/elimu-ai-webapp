package ai.elimu.web.content.emoji;

import ai.elimu.dao.EmojiDao;
import ai.elimu.dao.WordDao;
import ai.elimu.model.content.Emoji;
import ai.elimu.model.content.Word;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/content/emoji/edit")
@RequiredArgsConstructor
@Slf4j
public class EmojiEditController {

  private final EmojiDao emojiDao;

  private final WordDao wordDao;

  @RequestMapping(value = "/{id}", method = RequestMethod.GET)
  public String handleRequest(
      Model model,
      @PathVariable Long id) {
    log.info("handleRequest");

    Emoji emoji = emojiDao.read(id);
    model.addAttribute("emoji", emoji);

    List<Word> words = wordDao.readAllOrdered();
    model.addAttribute("words", words);
    model.addAttribute("emojisByWordId", getEmojisByWordId());

    return "content/emoji/edit";
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.POST)
  public String handleSubmit(
      @Valid Emoji emoji,
      BindingResult result,
      Model model) {
    log.info("handleSubmit");

    Emoji existingEmoji = emojiDao.readByGlyph(emoji.getGlyph());
    if ((existingEmoji != null) && !existingEmoji.getId().equals(emoji.getId())) {
      result.rejectValue("glyph", "NonUnique");
    }

    if (emoji.getUnicodeVersion() > 9) {
      result.rejectValue("glyph", "emoji.unicode.version");
    }

    if (result.hasErrors()) {
      model.addAttribute("emoji", emoji);

      List<Word> words = wordDao.readAllOrdered();
      model.addAttribute("words", words);
      model.addAttribute("emojisByWordId", getEmojisByWordId());

      return "content/emoji/edit";
    } else {
      emoji.setTimeLastUpdate(Calendar.getInstance());
      emoji.setRevisionNumber(emoji.getRevisionNumber() + 1);
      emojiDao.update(emoji);

      return "redirect:/content/emoji/list#" + emoji.getId();
    }
  }

  @RequestMapping(value = "/{id}/add-content-label", method = RequestMethod.POST)
  @ResponseBody
  public String handleAddContentLabelRequest(
      HttpServletRequest request,
      @PathVariable Long id) {
    log.info("handleAddContentLabelRequest");

    log.info("id: " + id);
    Emoji emoji = emojiDao.read(id);

    String wordIdParameter = request.getParameter("wordId");
    log.info("wordIdParameter: " + wordIdParameter);
    if (StringUtils.isNotBlank(wordIdParameter)) {
      Long wordId = Long.valueOf(wordIdParameter);
      Word word = wordDao.read(wordId);
      Set<Word> words = emoji.getWords();
      if (!words.contains(word)) {
        words.add(word);
        emoji.setRevisionNumber(emoji.getRevisionNumber() + 1);
        emojiDao.update(emoji);
      }
    }

    return "success";
  }

  @RequestMapping(value = "/{id}/remove-content-label", method = RequestMethod.POST)
  @ResponseBody
  public String handleRemoveContentLabelRequest(
      HttpServletRequest request,
      @PathVariable Long id) {
    log.info("handleRemoveContentLabelRequest");

    log.info("id: " + id);
    Emoji emoji = emojiDao.read(id);

    String wordIdParameter = request.getParameter("wordId");
    log.info("wordIdParameter: " + wordIdParameter);
    if (StringUtils.isNotBlank(wordIdParameter)) {
      Long wordId = Long.valueOf(wordIdParameter);
      Word word = wordDao.read(wordId);
      Set<Word> words = emoji.getWords();
      Iterator<Word> iterator = words.iterator();
      while (iterator.hasNext()) {
        Word existingWord = iterator.next();
        if (existingWord.getId().equals(word.getId())) {
          iterator.remove();
        }
      }
      emoji.setRevisionNumber(emoji.getRevisionNumber() + 1);
      emojiDao.update(emoji);
    }

    return "success";
  }

  private Map<Long, String> getEmojisByWordId() {
    log.info("getEmojisByWordId");

    Map<Long, String> emojisByWordId = new HashMap<>();

    for (Word word : wordDao.readAll()) {
      String emojiGlyphs = "";

      List<Emoji> emojis = emojiDao.readAllLabeled(word);
      for (Emoji emoji : emojis) {
        emojiGlyphs += emoji.getGlyph();
      }

      if (StringUtils.isNotBlank(emojiGlyphs)) {
        emojisByWordId.put(word.getId(), emojiGlyphs);
      }
    }

    return emojisByWordId;
  }
}
