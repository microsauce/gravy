package org.microsauce.gravy.util

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j

/**
 * Created with IntelliJ IDEA.
 * User: microsauce
 * Date: 3/6/13
 * Time: 7:56 PM
 * To change this template use File | Settings | File Templates.
 */
@Log4j
class MimeTable {

    private Map<String, String> fileExtensionMimeTypeMap = new HashMap<String, String>()

    MimeTable() {
        init()
    }

    @CompileStatic
    private void init() {
        log.info 'loading mime table . . .'
        InputStream is = MimeTable.class.getResourceAsStream('/mime.table')
        BufferedReader reader = new BufferedReader(new InputStreamReader(is))

        reader.eachLine { String line ->
            String[] tokens = line.split(' ')
            if (tokens.length > 1) {
                String typeIdentifier = tokens[0]
                for (int i = 1; i < tokens.length; i++)
                    fileExtensionMimeTypeMap[tokens[i]] = typeIdentifier
            }
        }
        log.info '\tcomplete'
    }

    @CompileStatic
    String mimeType(String fileExtension) {
        fileExtensionMimeTypeMap[fileExtension]
    }
}
