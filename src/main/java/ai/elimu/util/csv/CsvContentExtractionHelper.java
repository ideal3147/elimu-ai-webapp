package ai.elimu.util.csv;

import ai.elimu.dao.LetterDao;
import ai.elimu.dao.LetterSoundDao;
import ai.elimu.dao.SoundDao;
import ai.elimu.dao.WordDao;
import ai.elimu.model.content.Emoji;
import ai.elimu.model.content.Letter;
import ai.elimu.model.content.LetterSound;
import ai.elimu.model.content.Number;
import ai.elimu.model.content.Sound;
import ai.elimu.model.content.Word;
import ai.elimu.model.content.multimedia.Image;
import ai.elimu.model.enums.ContentLicense;
import ai.elimu.model.v2.enums.ReadingLevel;
import ai.elimu.model.v2.enums.content.ImageFormat;
import ai.elimu.model.v2.enums.content.SpellingConsistency;
import ai.elimu.model.v2.enums.content.WordType;
import ai.elimu.model.v2.gson.content.ImageGson;
import ai.elimu.model.v2.gson.content.StoryBookChapterGson;
import ai.elimu.model.v2.gson.content.StoryBookGson;
import ai.elimu.model.v2.gson.content.StoryBookParagraphGson;
import ai.elimu.web.content.emoji.EmojiCsvExportController;
import ai.elimu.web.content.letter_sound.LetterSoundCsvExportController;
import ai.elimu.web.content.multimedia.image.ImageCsvExportController;
import ai.elimu.web.content.number.NumberCsvExportController;
import ai.elimu.web.content.storybook.StoryBookCsvExportController;
import ai.elimu.web.content.word.WordCsvExportController;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.io.ClassRelativeResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class CsvContentExtractionHelper {

    /**
     * For information on how the CSV files were generated, see {@link LetterSoundCsvExportController#handleRequest}.
     */
    public static List<LetterSound> getLetterSoundsFromCsvBackup(File csvFile, LetterDao letterDao, SoundDao soundDao, LetterSoundDao letterSoundDao) {
        log.info("getLetterSoundsFromCsvBackup");

        List<LetterSound> letterSounds = new ArrayList<>();

        Path csvFilePath = Paths.get(csvFile.toURI());
        log.info("csvFilePath: " + csvFilePath);
        try {
            Reader reader = Files.newBufferedReader(csvFilePath);
            CSVFormat csvFormat = CSVFormat.DEFAULT
                    .withHeader(
                            "id",
                            "letter_ids",
                            "letter_texts",
                            "sound_ids",
                            "sound_values_ipa",
                            "usage_count"
                    )
                    .withSkipHeaderRecord();
            CSVParser csvParser = new CSVParser(reader, csvFormat);
            for (CSVRecord csvRecord : csvParser) {
                log.info("csvRecord: " + csvRecord);

                LetterSound letterSound = new LetterSound();

                JSONArray letterIdsJsonArray = new JSONArray(csvRecord.get("letter_ids"));
                log.debug("letterIdsJsonArray: " + letterIdsJsonArray);

                JSONArray letterTextsJsonArray = new JSONArray(csvRecord.get("letter_texts"));
                log.debug("letterTextsJsonArray: " + letterTextsJsonArray);
                List<Letter> letters = new ArrayList<>();
                for (int i = 0; i < letterTextsJsonArray.length(); i++) {
                    String letterText = letterTextsJsonArray.getString(i);
                    log.debug("Looking up Letter with text '" + letterText + "'");
                    Letter letter = letterDao.readByText(letterText);
                    log.debug("letter.getId(): " + letter.getId());
                    letters.add(letter);
                }
                letterSound.setLetters(letters);

                JSONArray soundIdsJsonArray = new JSONArray(csvRecord.get("sound_ids"));
                log.debug("soundIdsJsonArray: " + soundIdsJsonArray);

                JSONArray soundValuesIpaJsonArray = new JSONArray(csvRecord.get("sound_values_ipa"));
                log.debug("soundValuesIpaJsonArray: " + soundValuesIpaJsonArray);
                List<Sound> sounds = new ArrayList<>();
                for (int i = 0; i < soundValuesIpaJsonArray.length(); i++) {
                    String soundValueIpa = soundValuesIpaJsonArray.getString(i);
                    log.debug("Looking up Sound with IPA value /" + soundValueIpa + "/");
                    Sound sound = soundDao.readByValueIpa(soundValueIpa);
                    log.debug("sound.getId(): " + sound.getId());
                    sounds.add(sound);
                }
                letterSound.setSounds(sounds);

                Integer usageCount = Integer.valueOf(csvRecord.get("usage_count"));
                letterSound.setUsageCount(usageCount);

                letterSounds.add(letterSound);
            }
        } catch (IOException ex) {
            log.error(ex.getMessage());
        }

        return letterSounds;
    }

    /**
     * For information on how the CSV files were generated, see {@link WordCsvExportController#handleRequest}.
     */
    public static List<Word> getWordsFromCsvBackup(File csvFile, LetterDao letterDao, SoundDao soundDao, LetterSoundDao letterSoundDao, WordDao wordDao) {
        log.info("getWordsFromCsvBackup");

        List<Word> words = new ArrayList<>();

        Path csvFilePath = Paths.get(csvFile.toURI());
        log.info("csvFilePath: " + csvFilePath);
        try {
            Reader reader = Files.newBufferedReader(csvFilePath);
            CSVFormat csvFormat = CSVFormat.DEFAULT
                    .withHeader(
                            "id",
                            "text",
                            "letter_sound_correspondences",
                            "usage_count",
                            "word_type",
                            "spelling_consistency",
                            "root_word_id",
                            "root_word_text"
                    )
                    .withSkipHeaderRecord();
            CSVParser csvParser = new CSVParser(reader, csvFormat);
            for (CSVRecord csvRecord : csvParser) {
                log.info("csvRecord: " + csvRecord);

                Word word = new Word();

                String text = csvRecord.get("text");
                word.setText(text);

                JSONArray letterSoundsJsonArray = new JSONArray(csvRecord.get("letter_sound_correspondences"));
                log.debug("letterSoundsJsonArray: " + letterSoundsJsonArray);
                List<LetterSound> letterSounds = new ArrayList<>();
                for (int i = 0; i < letterSoundsJsonArray.length(); i++) {
                    JSONObject letterSoundJsonObject = letterSoundsJsonArray.getJSONObject(i);
                    log.debug("letterSoundJsonObject: " + letterSoundJsonObject);
                    List<Letter> letters = new ArrayList<>();
                    JSONArray lettersJsonArray = letterSoundJsonObject.getJSONArray("letters");
                    for (int j = 0; j < lettersJsonArray.length(); j++) {
                        Letter letter = letterDao.readByText(lettersJsonArray.getString(j));
                        letters.add(letter);
                    }
                    List<Sound> sounds = new ArrayList<>();
                    JSONArray soundsJsonArray = letterSoundJsonObject.getJSONArray("sounds");
                    for (int j = 0; j < soundsJsonArray.length(); j++) {
                        Sound sound = soundDao.readByValueIpa(soundsJsonArray.getString(j));
                        sounds.add(sound);
                    }
                    LetterSound letterSound = letterSoundDao.read(letters, sounds);
                    log.debug("letterSound.getId(): " + letterSound.getId());
                    letterSounds.add(letterSound);
                }
                word.setLetterSounds(letterSounds);

                Integer usageCount = Integer.valueOf(csvRecord.get("usage_count"));
                word.setUsageCount(usageCount);

                if (StringUtils.isNotBlank(csvRecord.get("word_type"))) {
                    WordType wordType = WordType.valueOf(csvRecord.get("word_type"));
                    word.setWordType(wordType);
                }

                if (StringUtils.isNotBlank(csvRecord.get("spelling_consistency"))) {
                    SpellingConsistency spellingConsistency = SpellingConsistency.valueOf(csvRecord.get("spelling_consistency"));
                    word.setSpellingConsistency(spellingConsistency);
                }

                // TODO: Store rootWords _after_ all Words have been stored
//                if (StringUtils.isNotBlank(csvRecord.get("root_word_text"))) {
//                    String rootWordText = csvRecord.get("root_word_text");
//                    Word rootWord = wordDao.readByText(language, rootWordText);
//                    word.setRootWord(rootWord);
//                }

                words.add(word);
            }
        } catch (IOException ex) {
            log.error(ex.getMessage());
        }

        return words;
    }

    /**
     * For information on how the CSV files were generated, see {@link NumberCsvExportController#handleRequest}.
     */
    public static List<Number> getNumbersFromCsvBackup(File csvFile, WordDao wordDao) {
        log.info("getNumbersFromCsvBackup");

        List<Number> numbers = new ArrayList<>();

        Path csvFilePath = Paths.get(csvFile.toURI());
        log.info("csvFilePath: " + csvFilePath);
        try {
            Reader reader = Files.newBufferedReader(csvFilePath);
            CSVFormat csvFormat = CSVFormat.DEFAULT
                    .withHeader(
                            "id",
                            "value",
                            "symbol",
                            "word_ids",
                            "word_texts"
                    )
                    .withSkipHeaderRecord();
            CSVParser csvParser = new CSVParser(reader, csvFormat);
            for (CSVRecord csvRecord : csvParser) {
                log.info("csvRecord: " + csvRecord);

                Number number = new Number();

                Integer value = Integer.valueOf(csvRecord.get("value"));
                number.setValue(value);

                String symbol = csvRecord.get("symbol");
                number.setSymbol(symbol);

                JSONArray wordIdsJsonArray = new JSONArray(csvRecord.get("word_ids"));
                log.info("wordIdsJsonArray: " + wordIdsJsonArray);

                JSONArray wordTextsJsonArray = new JSONArray(csvRecord.get("word_texts"));
                log.info("wordTextsJsonArray: " + wordTextsJsonArray);
                List<Word> words = new ArrayList<>();
                for (int i = 0; i < wordTextsJsonArray.length(); i++) {
                    String wordText = wordTextsJsonArray.getString(i);
                    log.info("Looking up Word with text /" + wordText + "/");
                    Word word = wordDao.readByText(wordText);
                    log.info("word.getId(): \"" + word.getId() + "\"");
                    words.add(word);
                }
                number.setWords(words);

                numbers.add(number);
            }
        } catch (IOException ex) {
            log.error(ex.getMessage());
        }

        return numbers;
    }

    /**
     * For information on how the CSV files were generated, see {@link EmojiCsvExportController#handleRequest}.
     */
    public static List<Emoji> getEmojisFromCsvBackup(File csvFile, WordDao wordDao) {
        log.info("getEmojisFromCsvBackup");

        List<Emoji> emojis = new ArrayList<>();

        Path csvFilePath = Paths.get(csvFile.toURI());
        log.info("csvFilePath: " + csvFilePath);
        try {
            Reader reader = Files.newBufferedReader(csvFilePath);
            CSVFormat csvFormat = CSVFormat.DEFAULT
                    .withHeader(
                            "id",
                            "glyph",
                            "unicode_version",
                            "unicode_emoji_version",
                            "word_ids",
                            "word_texts"
                    )
                    .withSkipHeaderRecord();
            CSVParser csvParser = new CSVParser(reader, csvFormat);
            for (CSVRecord csvRecord : csvParser) {
                log.info("csvRecord: " + csvRecord);

                Emoji emoji = new Emoji();

                String glyph = csvRecord.get("glyph");
                emoji.setGlyph(glyph);

                Double unicodeVersion = Double.valueOf(csvRecord.get("unicode_version"));
                emoji.setUnicodeVersion(unicodeVersion);

                Double unicodeEmojiVersion = Double.valueOf(csvRecord.get("unicode_emoji_version"));
                emoji.setUnicodeEmojiVersion(unicodeEmojiVersion);

                JSONArray wordIdsJsonArray = new JSONArray(csvRecord.get("word_ids"));
                log.info("wordIdsJsonArray: " + wordIdsJsonArray);

                JSONArray wordTextsJsonArray = new JSONArray(csvRecord.get("word_texts"));
                log.info("wordTextsJsonArray: " + wordTextsJsonArray);
                Set<Word> words = new HashSet<>();
                for (int i = 0; i < wordTextsJsonArray.length(); i++) {
                    String wordText = wordTextsJsonArray.getString(i);
                    log.info("Looking up Word with text /" + wordText + "/");
                    Word word = wordDao.readByText(wordText);
                    log.info("word.getId(): \"" + word.getId() + "\"");
                    words.add(word);
                }
                emoji.setWords(words);

                emojis.add(emoji);
            }
        } catch (IOException ex) {
            log.error(ex.getMessage());
        }

        return emojis;
    }

    /**
     * For information on how the CSV files were generated, see {@link ImageCsvExportController#handleRequest}.
     */
    public static List<Image> getImagesFromCsvBackup(File csvFile) {
        log.info("getImagesFromCsvBackup");

        List<Image> images = new ArrayList<>();

        Path csvFilePath = Paths.get(csvFile.toURI());
        log.info("csvFilePath: " + csvFilePath);
        try {
            Reader reader = Files.newBufferedReader(csvFilePath); 
            CSVFormat csvFormat = CSVFormat.DEFAULT
                    .withHeader(
                            "id",
                            "content_type",
                            "content_license",
                            "attribution_url",
                            "title",
                            "cid",
                            "download_url",
                            "image_format"
                    )
                    .withSkipHeaderRecord();
            CSVParser csvParser = new CSVParser(reader, csvFormat);
            for (CSVRecord csvRecord : csvParser) {
                log.info("csvRecord: " + csvRecord);

                Image image = new Image();

                String contentType = csvRecord.get("content_type");
                image.setContentType(contentType);

                if (StringUtils.isNotBlank(csvRecord.get("content_license"))) {
                    ContentLicense contentLicense = ContentLicense.valueOf(csvRecord.get("content_license"));
                    image.setContentLicense(contentLicense);
                }

                String attributionUrl = csvRecord.get("attribution_url");
                image.setAttributionUrl(attributionUrl);

                String title = csvRecord.get("title");
                image.setTitle(title);

                ResourceLoader resourceLoader = new ClassRelativeResourceLoader(CsvContentExtractionHelper.class);
                Resource resource = resourceLoader.getResource("placeholder.png");
                File bytesFile = resource.getFile();
                Path bytesPath = bytesFile.toPath();
                byte[] bytes = Files.readAllBytes(bytesPath);
                image.setBytes(bytes);

                ImageFormat imageFormat = ImageFormat.valueOf(csvRecord.get("image_format"));
                image.setImageFormat(imageFormat);

                images.add(image);
            }
        } catch (IOException ex) {
            log.error(ex.getMessage());
        }

        return images;
    }

    /**
     * For information on how the CSV files were generated, see {@link StoryBookCsvExportController#handleRequest}.
     * <p />
     * Also see {@link #getStoryBookChaptersFromCsvBackup}
     */
    public static List<StoryBookGson> getStoryBooksFromCsvBackup(File csvFile) {
        log.info("getStoryBooksFromCsvBackup");

        List<StoryBookGson> storyBookGsons = new ArrayList<>();

        Path csvFilePath = Paths.get(csvFile.toURI());
        log.info("csvFilePath: " + csvFilePath);
        try {
            Reader reader = Files.newBufferedReader(csvFilePath);
            CSVFormat csvFormat = CSVFormat.DEFAULT
                    .withHeader(
                            "id",
                            "title",
                            "description",
                            "content_license",
                            "attribution_url",
                            "reading_level",
                            "cover_image_id",
                            "chapters"
                    )
                    .withSkipHeaderRecord();
            CSVParser csvParser = new CSVParser(reader, csvFormat);
            for (CSVRecord csvRecord : csvParser) {
                log.debug("csvRecord: " + csvRecord);

                // Convert from CSV to GSON

                StoryBookGson storyBookGson = new StoryBookGson();

                String title = csvRecord.get("title");
                log.info("title: \"" + title + "\"");
                storyBookGson.setTitle(title);

                String description = csvRecord.get("description");
                storyBookGson.setDescription(description);

                if (StringUtils.isNotBlank(csvRecord.get("content_license"))) {
                    ContentLicense contentLicense = ContentLicense.valueOf(csvRecord.get("content_license"));
//                    storyBookGson.setContentLicense(contentLicense);
                }

                String attributionUrl = csvRecord.get("attribution_url");
//                storyBookGson.setAttributionUrl(attributionUrl);

                if (StringUtils.isNotBlank(csvRecord.get("reading_level"))) {
                    ReadingLevel readingLevel = ReadingLevel.valueOf(csvRecord.get("reading_level"));
                    storyBookGson.setReadingLevel(readingLevel);
                }

                if (StringUtils.isNotBlank(csvRecord.get("cover_image_id"))) {
                    Long coverImageId = Long.valueOf(csvRecord.get("cover_image_id"));
                    ImageGson coverImageGson = new ImageGson();
                    coverImageGson.setId(coverImageId);
                   storyBookGson.setCoverImage(coverImageGson);
                }

                List<StoryBookChapterGson> storyBookChapterGsons = new ArrayList<>();
                JSONArray chaptersJsonArray = new JSONArray(csvRecord.get("chapters"));
                log.debug("chaptersJsonArray: " + chaptersJsonArray);
                for (int i = 0; i < chaptersJsonArray.length(); i++) {
                    JSONObject chapterJsonObject = chaptersJsonArray.getJSONObject(i);
                    log.debug("chapterJsonObject: " + chapterJsonObject);

                    StoryBookChapterGson storyBookChapterGson = new StoryBookChapterGson();
                    storyBookChapterGson.setSortOrder(chapterJsonObject.getInt("sortOrder"));

                    if (chapterJsonObject.has("image")) {
                        JSONObject chapterImageJsonObject = chapterJsonObject.getJSONObject("image");
                        log.debug("chapterImageJsonObject: " + chapterImageJsonObject);
                        ImageGson chapterImageGson = new ImageGson();
                        chapterImageGson.setId(chapterImageJsonObject.getLong("id"));
                        storyBookChapterGson.setImage(chapterImageGson);
                    }

                    List<StoryBookParagraphGson> storyBookParagraphGsons = new ArrayList<>();
                    JSONArray paragraphsJsonArray = chapterJsonObject.getJSONArray("storyBookParagraphs");
                    log.debug("paragraphsJsonArray: " + paragraphsJsonArray);
                    for (int j = 0; j < paragraphsJsonArray.length(); j++) {
                        JSONObject paragraphJsonObject = paragraphsJsonArray.getJSONObject(j);
                        log.debug("paragraphJsonObject: " + paragraphJsonObject);

                        StoryBookParagraphGson storyBookParagraphGson = new StoryBookParagraphGson();
                        storyBookParagraphGson.setSortOrder(paragraphJsonObject.getInt("sortOrder"));
                        storyBookParagraphGson.setOriginalText(paragraphJsonObject.getString("originalText"));
                        // TODO: setWords

                        storyBookParagraphGsons.add(storyBookParagraphGson);
                    }
                    storyBookChapterGson.setStoryBookParagraphs(storyBookParagraphGsons);

                    storyBookChapterGsons.add(storyBookChapterGson);
                }
                storyBookGson.setStoryBookChapters(storyBookChapterGsons);

                storyBookGsons.add(storyBookGson);
            }
        } catch (IOException ex) {
            log.error(ex.getMessage());
        }

        return storyBookGsons;
    }
}
