package ai.elimu.model.contributor;

import ai.elimu.model.content.Sound;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class SoundContributionEvent extends ContributionEvent {

  @NotNull
  @ManyToOne
  private Sound sound;
}
