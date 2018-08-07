package jakubgrzaslewicz.pl.roomassetreadonly

import android.util.Log
import java.util.Comparator
import java.util.regex.Pattern

internal class VersionComparator : Comparator<String> {

    private val pattern = Pattern
            .compile(".*_upgrade_([0-9]+)-([0-9]+).*")

    /**
     * Compares the two specified upgrade script strings to determine their
     * relative ordering considering their two version numbers. Assumes all
     * database names used are the same, as this function only compares the
     * two version numbers.
     *
     * @param file0
     * an upgrade script file name
     * @param file1
     * a second upgrade script file name to compare with file0
     * @return an integer < 0 if file0 should be applied before file1, 0 if
     * they are equal (though that shouldn't happen), and > 0 if
     * file0 should be applied after file1.
     *
     * @exception SQLiteAssetException
     * thrown if the strings are not in the correct upgrade
     * script format of:
     * `databasename_fromVersionInteger_toVersionInteger`
     */
    override fun compare(file0: String, file1: String): Int {
        val m0 = pattern.matcher(file0)
        val m1 = pattern.matcher(file1)

        if (!m0.matches()) {
            Log.w(TAG, "could not parse upgrade script file: $file0")
            throw SQLiteAssetROHelper.SQLiteAssetException("Invalid upgrade script file")
        }

        if (!m1.matches()) {
            Log.w(TAG, "could not parse upgrade script file: $file1")
            throw SQLiteAssetROHelper.SQLiteAssetException("Invalid upgrade script file")
        }

        val v0_from = Integer.valueOf(m0.group(1))
        val v1_from = Integer.valueOf(m1.group(1))
        val v0_to = Integer.valueOf(m0.group(2))
        val v1_to = Integer.valueOf(m1.group(2))

        if (v0_from == v1_from) {
            // 'from' versions match for both; check 'to' version next

            if (v0_to == v1_to) {
                return 0
            }

            return if (v0_to < v1_to) -1 else 1
        }

        return if (v0_from < v1_from) -1 else 1
    }

    companion object {

        private val TAG = SQLiteAssetROHelper.TAG
    }
}