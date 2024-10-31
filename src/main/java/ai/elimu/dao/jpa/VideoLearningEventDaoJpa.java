package ai.elimu.dao.jpa;

import ai.elimu.dao.VideoLearningEventDao;
import ai.elimu.dao.enums.OrderDirection;
import ai.elimu.model.analytics.VideoLearningEvent;
import java.util.Calendar;
import java.util.List;

import javax.persistence.NoResultException;
import org.springframework.dao.DataAccessException;

public class VideoLearningEventDaoJpa extends GenericDaoJpa<VideoLearningEvent> implements VideoLearningEventDao {

    @Override
    public VideoLearningEvent read(Calendar timestamp, String androidId, String packageName, String videoTitle) throws DataAccessException {
        try {
            return (VideoLearningEvent) em.createQuery(
                "SELECT event " +
                "FROM VideoLearningEvent event " +
                "WHERE event.timestamp = :timestamp " +
                "AND event.androidId = :androidId " + 
                "AND event.packageName = :packageName " + 
                "AND event.videoTitle = :videoTitle")
                .setParameter("timestamp", timestamp)
                .setParameter("androidId", androidId)
                .setParameter("packageName", packageName)
                .setParameter("videoTitle", videoTitle)
                .getSingleResult();
        } catch (NoResultException e) {
            logger.info("VideoLearningEvent (" + timestamp.getTimeInMillis() + ", " + androidId + ", " + packageName + ", \"" + videoTitle + "\") was not found");
            return null;
        }
    }

    @Override
    public List<VideoLearningEvent> readAllOrderedByTimestamp(OrderDirection orderDirection) throws DataAccessException {
        return em.createQuery(
            "SELECT event " + 
            "FROM VideoLearningEvent event " +
            "ORDER BY event.timestamp " + orderDirection)
            .getResultList();
    }
}
