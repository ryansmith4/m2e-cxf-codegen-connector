package org.eclipselabs.m2e.cxf.codegen.connector;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecution;
import org.codehaus.plexus.util.Scanner;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.project.configurator.MojoExecutionBuildParticipant;
import org.sonatype.plexus.build.incremental.BuildContext;
  
public class CxfWsdl2JavaBuildParticipant extends MojoExecutionBuildParticipant
{
  
    public CxfWsdl2JavaBuildParticipant( MojoExecution execution )
    {
        super( execution, true );
    }

	@Override
    public Set<IProject> build( int kind, IProgressMonitor monitor ) throws Exception
    {
  
        final IMaven maven = MavenPlugin.getMaven();
        final BuildContext buildContext = getBuildContext();
        final MavenSession mavenSession = getSession();
        final MojoExecution mojoExecution = getMojoExecution();
  
        File sourceRoot = maven.getMojoParameterValue( mavenSession, mojoExecution, "sourceRoot",
                                                       File.class );
  
        boolean filesModified = false;
  
        if ( sourceRoot != null && ( !sourceRoot.exists() || 
        		IncrementalProjectBuilder.CLEAN_BUILD == kind || IncrementalProjectBuilder.FULL_BUILD == kind ) )
        {
            filesModified = true;
        }
        else
        {
  
            final List<?> wsdlOptions = maven.getMojoParameterValue( mavenSession, mojoExecution,
                                                                  "wsdlOptions", List.class );
  
            // getMojoParameterValue returns an instance of WsdlOption from a different classloader, so casting doesn't work.
            for ( Object obj : wsdlOptions )
            {
                Class<? extends Object> k = obj.getClass();
                Method getWsdl = k.getMethod( "getWsdl" );
                Method getBindingFiles = k.getMethod( "getBindingFiles" );
  
                String wsdl = getWsdl.invoke( obj ).toString();
  
                filesModified = ( !isEmpty( wsdl ) && !isEmpty( getModifiedFiles( buildContext, new File( wsdl ) ) ) );
  
                if ( !filesModified )
                {
                	String[] bindingFiles = null;
                	Object param = getBindingFiles.invoke( obj );
                	
                	// CXF 2.5.10+ use HashSet to store bindingFiles attribute
                	if (param instanceof Set)
                	{
                		@SuppressWarnings("unchecked")
						Set<String> bindingFilesSet = (Set<String>)param;
                		bindingFiles = bindingFilesSet.toArray(new String[bindingFilesSet.size()]);
                	}
                	else
                	{
                		bindingFiles = (String[]) param;
                	}

                	for ( String bindingFile : bindingFiles )
                    {
                        filesModified = ( !isEmpty( bindingFile ) &&
                                          !isEmpty( getModifiedFiles( buildContext, new File( bindingFile ) ) ) );
  
                        if ( filesModified )
                        {
                            break;
                        }
                    }
                }
  
                if ( filesModified )
                {
                    break;
                }
            }
  
        }
  
        if ( !filesModified )
        {
            return null;
        }
  
        final Set<IProject> result = super.build( kind, monitor );
  
        if ( sourceRoot != null )
        {
            buildContext.refresh( sourceRoot );
        }
  
        return result;
    }
	
	public static <T> boolean isEmpty( final T[] array )
	{
		return array == null || array.length == 0;
	}
   
	public static boolean isEmpty(String str) {
        return (str == null || str.length() == 0);
    }
	
    public static String[] getModifiedFiles( BuildContext buildContext, File source )
        throws Exception
    {
        if ( buildContext == null || source == null || !source.exists() )
        {
            return null;
        }
  
        final Scanner ds = buildContext.newScanner( source );
  
        ds.scan();
  
        return ds.getIncludedFiles();
    }
}