package org.eclipselabs.m2e.cxf.codegen.connector;

import org.apache.maven.plugin.MojoExecution;
import org.eclipse.m2e.core.lifecyclemapping.model.IPluginExecutionMetadata;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractBuildParticipant;
import org.eclipse.m2e.jdt.AbstractJavaProjectConfigurator;

/**
*
*/
public class CxfWsdl2JavaProjectConfigurator extends
		AbstractJavaProjectConfigurator {

	@Override
	protected String getOutputFolderParameterName() {
		return "sourceRoot";
	}

	@Override
	public AbstractBuildParticipant getBuildParticipant(
			IMavenProjectFacade projectFacade, MojoExecution execution,
			IPluginExecutionMetadata executionMetadata) {
		return new CxfWsdl2JavaBuildParticipant(execution);
	}
}