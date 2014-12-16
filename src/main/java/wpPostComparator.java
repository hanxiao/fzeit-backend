import java.util.Comparator;

/**
 * Created by hxiao on 12/13/14.
 */
class wpPostComparator implements Comparator<wpPost> {
    public int compare(wpPost m1, wpPost m2) {
        return m1.pub_date.compareTo(m2.pub_date);
    }
}
