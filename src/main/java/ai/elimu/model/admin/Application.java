package ai.elimu.model.admin;

import ai.elimu.model.BaseEntity;
import ai.elimu.model.contributor.Contributor;
import ai.elimu.model.v2.enums.admin.ApplicationStatus;
import ai.elimu.model.v2.enums.content.LiteracySkill;
import ai.elimu.model.v2.enums.content.NumeracySkill;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Application extends BaseEntity {

  @NotNull
  private String packageName;

  private boolean infrastructural;

  @ElementCollection(fetch = FetchType.EAGER)
  @Enumerated(EnumType.STRING)
  private Set<LiteracySkill> literacySkills;

  @ElementCollection(fetch = FetchType.EAGER)
  @Enumerated(EnumType.STRING)
  private Set<NumeracySkill> numeracySkills;

  @NotNull
  @Enumerated(EnumType.STRING)
  private ApplicationStatus applicationStatus;

  @NotNull
  @ManyToOne
  private Contributor contributor;
}
