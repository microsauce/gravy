package org.microsauce.gravy.dev

class DevUtils {

    private static String GRAVY = '.gravy'
    private static String DEPLOY = 'deploy'

    static String deployRoot() {
        String userHome = System.getProperty('user.home')
        userHome + '/' + GRAVY + '/' + DEPLOY
    }

    static String appDeployPath(String projectPath) {
        File projectFile = new File(projectPath)
        String projectName = projectFile.name
        deployRoot() + '/' + projectName
    }
}