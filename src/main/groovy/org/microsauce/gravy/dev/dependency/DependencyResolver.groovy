package com.microsauce.gravy.dev.dependency

import org.apache.ivy.Ivy
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor
import org.apache.ivy.core.module.id.ModuleRevisionId
import org.apache.ivy.core.report.ResolveReport
import org.apache.ivy.core.resolve.ResolveOptions
import org.apache.ivy.core.retrieve.RetrieveOptions
import org.apache.ivy.core.settings.IvySettings
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorWriter
import org.apache.ivy.plugins.repository.url.URLRepository;
import org.apache.ivy.plugins.resolver.ChainResolver
import org.apache.ivy.plugins.resolver.URLResolver

class DependencyResolver {
    
    private static final mavenPattern = ~ /(.+)-[0-9\.]+/
    
    boolean returnFirst = false
    
    String projectLibPath
    String projectModPath
    String uriPattern = '/[organisation]/[module]/[revision]/[artifact](-[revision]).[ext]'
    
    private List<String> resolverUrls = []
    private projectBase = null
    private ChainResolver chain
    private AntBuilder ant

    DependencyResolver(String projectBase) { 
        this.projectBase = projectBase
        this.returnFirst = false
        
        ant = new AntBuilder()
    }

    File resolveDependency(String coordinates) {
        if ( coordinates == null || coordinates.length() == 0 ) return null
        
        String[] coords = coordinates.split(':')
        if ( coords.length != 3 ) 
            throw new Exception('Invalid maven coordinates.  They must be in the form \'group:artifactId:version\'')
        
        resolveArtifact(
            coords[0] /*group*/, 
            coords[1] /*artifactId*/, 
            coords[2] /*version*/)
    }

    File installDependency(String coordinates, String installPath) {
        File file = resolveDependency(coordinates)
        File installFolder = new File(installPath)
        if ( !installFolder.exists() )
            installFolder.mkdirs()
        
        ant.copy(file:"${file.absolutePath}", todir:"${installPath}")
        new File("${installPath}/${file.name}")
    }
    
    void installModule(String coordinates, String installPath) {
        File file = resolveDependency(coordinates)
        extractModule(moduleJar, installPath)
    }
    
    void extractModule(File moduleJar, String installPath) {
        String fileName = moduleJar.name
        String moduleName = null
        if ( fileName ==~ mavenPattern ) {
            def matches = fileName =~ mavenPattern
            moduleName = matches[0][1]
        }

        ant.unzip(src:"${moduleJar}", dest:"${installPath}+'/'+${moduleName}")
    }

    private void addResolver(String url) {
        resolverUrls.add url
    }

    private URLResolver newURLResolver(String name, String url) {
        URLResolver resolver = new URLResolver()
        resolver.setM2compatible(true)
        resolver.name = name
        resolver.addArtifactPattern url+uriPattern
        resolver
    }
    
    private File resolveArtifact(String groupId, String artifactId, String version) throws Exception {

        IvySettings ivySettings = new IvySettings()
        ChainResolver chainResolver = new ChainResolver()
        chainResolver.returnFirst = returnFirst // false by default
        chainResolver.name = 'chain'
        def ndx = 0
        resolverUrls.each { url -> 
            chainResolver.add(newURLResolver('repository'+(ndx++), url)) 
        }
        chainResolver.add(newURLResolver('repository'+(ndx++), 'http://repo1.maven.org/maven2'))  // add a base resolver
        ivySettings.addResolver chainResolver
        ivySettings.setDefaultResolver('chain')
        Ivy ivy = Ivy.newInstance(ivySettings)

        File ivyfile = File.createTempFile('ivy', '.xml')
        ivyfile.deleteOnExit()

        String[] dep = null;
        dep = [groupId, artifactId, version]

        DefaultModuleDescriptor md =
                DefaultModuleDescriptor.newDefaultInstance(
                    ModuleRevisionId.newInstance(dep[0], dep[1]+'-caller', 'working'))

        DefaultDependencyDescriptor dd = new DefaultDependencyDescriptor(md,
                ModuleRevisionId.newInstance(dep[0], dep[1], dep[2]), false, false, true)
        md.addDependency(dd)

        XmlModuleDescriptorWriter.write(md, ivyfile)
        String[] confs = ['default']
        ResolveOptions resolveOptions = new ResolveOptions().setConfs(confs)
        ResolveReport report = ivy.resolve(ivyfile.toURI().toURL(), resolveOptions)
    
        if ( report.getAllArtifactsReports().length == 0 ) 
            throw new Exception("Unable to resolve dependency $groupId:$artifactId:$version")
        
        File jarArtifactFile = report.getAllArtifactsReports()[0].getLocalFile()
        return jarArtifactFile;
    }

}