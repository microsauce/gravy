package org.microsauce.gravy.lang.javascript

	class ResolvedResource {
		String absolutePath
		String resolvedUri
		String text
		Long lastModified
		Boolean js
		
		ResolvedResource(String resolvedUri, String absolutePath, String text, Long lastModified) {
			this.resolvedUri = resolvedUri
			this.absolutePath = absolutePath
			this.text = text
			this.lastModified = lastModified
		}
		
		boolean isFound() {
			text != null
		}
		
		boolean isJS() {
			if ( js == null ) js = resolvedUri.endsWith('.js')
			js
		}
	}

