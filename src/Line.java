

/**
 * A class used to parse the happify data set .csv file
 * @author skwaugh
 *
 */
public class Line {
	

	String user_id;	
	String message_id;	
	String created_at;	
	// tip	
	String short_text_1;
	//String short_text_2;	
	//String short_text_3;	
	// short_text_4	
	// short_text_5	
	// message	
	// happy_face	
	// track_id	
	// challenge_id	
	String gender;	
	String age_range;	
	//String employment_status;

	
	public Line(String line) {
		
		String[] pieces = line.split("\t");
		int i = 0;
		user_id	= pieces[i++];
		message_id = pieces[i++];
		created_at	= pieces[i++];
		i++; // tip	
		short_text_1 = pieces[i++];
		//short_text_2 = pieces[i++];
		//short_text_3 = pieces[i++];
		//i++; // short_text_4	
		//i++; // short_text_5	
		//i++; // message	
		//i++; // happy_face	
		//i++; // track_id	
		//i++; // challenge_id	
		//gender = pieces[i++];
		//age_range =	pieces[i++];
		//employment_status = pieces[i++]; 
	}
	
	/*public String toString() {
		return user_id + "\t" + message_id + "\t" + created_at + "\t" + short_text_1 + "\t" +
				short_text_2 + "\t" + short_text_3 + "\t" + gender + 
				"\t" + age_range + "\t" + employment_status + "\n";
	}*/
}
