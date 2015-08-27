/**
 * a class used to describe a Happify user
 * @author skwaugh
 *
 */
public class User {

	String id;
	String user_id;
	String happiness_score;
	String created_at;
	String positive_score;
	String satisfaction_score;

	
	public User(String[] fields) {
		id = fields[0];
		user_id = fields[1];
		happiness_score = fields[2];
		created_at = fields[3];
		positive_score = fields[4];
		satisfaction_score = fields[5];

	}
	
	
	
}
