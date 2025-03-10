package ai.elimu.model.content.multimedia;

import ai.elimu.model.content.StoryBookParagraph;
import ai.elimu.model.content.Word;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Deprecated
@Getter
@Setter
@Entity
public class Audio extends Multimedia {

  /**
   * Will be used if the Audio recording was made for a particular {@link Word}.
   */
  @ManyToOne
  private Word word;

  /**
   * Will be used if the Audio recording was made for a particular {@link StoryBookParagraph}.
   */
  @ManyToOne
  private StoryBookParagraph storyBookParagraph;

  /**
   * A title describing the audio recording. This does not have match the audio's actual content.
   */
  @NotNull
  private String title;

  /**
   * The actual content of the audio recording.
   */
  @NotNull
  private String transcription;

  @NotNull
  @Lob
  @Column(length = 209715200) // 200MB
  private byte[] bytes;

  /**
   * The duration of the audio recording in milliseconds.
   */
  private Long durationMs;

  @NotNull
  private String audioFormat;
}
