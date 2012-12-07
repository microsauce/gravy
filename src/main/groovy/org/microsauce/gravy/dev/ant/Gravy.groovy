package org.microsauce.gravy.dev.ant

import org.apache.tools.ant.Task
import org.apache.tools.ant.Project
import org.microsauce.gravy.dev.lifecycle.Lifecycle;

class Gravy extends Task {

	Set<String> noArgGoals 
	Set<String> allGoals 

	//
	// life-cycle goals
	// 
	static String CLEAN 	= 'clean'
	static String RESOLVE	= 'resolve'
	static String COMPILE   = 'compile'
	static String TEST  	= 'test'
	static String ASSEMBLE  = 'assemble'
	static String WAR 		= 'war'		// takes args
	static String DEPLOY 	= 'deploy'	// takes args
	static String PUBLISH = 'publish'	// TODO
	
	//
	// tools
	//
	static String MOD_IFY = 'modIfy'
	static String JAR_MOD = 'jarMod'
	

	String projectPath
	String deployPath
	String goal = DEPLOY 				// default goal
	String skipTests = 'false'
	String warName = null

	Gravy() {

		goals = new HashSet<String>()
		noArgGoals = new HashSet<String>()

		noArgGoals << CLEAN
		noArgGoals << RESOLVE
		noArgGoals << COMPILE
		noArgGoals << TEST
		noArgGoals << ASSEMBLE
		noArgGoals << MOD_IFY

		goals.addAll noArgGoals
		goals << WAR
		goals << DEPLOY
		goals << PUBLISH
	}

	void execute() {
		if ( !goals.contains(goal) ) {
			log( "executing gravy goal $goal", Project.MSG_INFO )
			Lifecycle lifecycle = new Lifecycle()
			if ( !noArgGoals.contains(goal) ) {
				lifecycle[goal]()
			} else if ( goal == WAR ) {
				lifecycle.war(warName, _skipTests())
			} else if ( goal == DEPLOY ) {
				lifecycle.deploy(warName, deployPath, _skipTests())
			} else if ( goal == PUBLISH ) {
				lifecycle.publish(deployPath)
			}
		} else {
			log( "unknown goal: $goal", Project.MSG_ERR )
		}
	}

	private boolean _skipTests() {
		if (!skipTests) return false

		Boolean.parseBoolean(skipTests)
	}
}