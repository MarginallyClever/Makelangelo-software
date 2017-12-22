package com.marginallyclever.makelangelo;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;

@SuppressWarnings("serial")
public class TestGLJPanel extends GLJPanel implements GLEventListener {
	public TestGLJPanel(GLCapabilities caps) throws GLException {
		super(caps);
        addGLEventListener(this);
	}
	
    @Override
    public void reshape( GLAutoDrawable glautodrawable, int x, int y, int width, int height ) {
        OneTriangle.setup( glautodrawable.getGL().getGL2(), width, height );
    }
    
    @Override
    public void init( GLAutoDrawable glautodrawable ) {
    }
    
    @Override
    public void dispose( GLAutoDrawable glautodrawable ) {
    }
    
    @Override
    public void display( GLAutoDrawable glautodrawable ) {
        OneTriangle.render( glautodrawable.getGL().getGL2(), glautodrawable.getSurfaceWidth(), glautodrawable.getSurfaceHeight() );
    }

	public static void main(String[] argv) {
        GLProfile glprofile = GLProfile.getDefault();
        GLCapabilities glcapabilities = new GLCapabilities( glprofile );
        TestGLJPanel gljpanel = new TestGLJPanel( glcapabilities ); 

        final JFrame jframe = new JFrame( "One Triangle Swing GLJPanel" ); 
        jframe.addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent windowevent ) {
                jframe.dispose();
                System.exit( 0 );
            }
        });

		Splitter splitter = new Splitter(JSplitPane.VERTICAL_SPLIT);
		splitter.add(gljpanel);

		splitter.setResizeWeight(0.9);
		splitter.setOneTouchExpandable(true);
		splitter.setDividerLocation(400);
		
        jframe.getContentPane().add( splitter, BorderLayout.CENTER );
        jframe.setSize( 640, 480 );
        jframe.setVisible( true );
	}
}
