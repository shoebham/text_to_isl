/*
 * Wrap.java		2007-05-19
 */
package app.gui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Box;


/** {@code Wrap} provides a collection of class methods each of which
 * wraps a new container round given collection of GUI components.
 */
public final class Wrap {

/** Wraps the given list of components in a vertical box, wraps the box
 * in a flow layout pane, and returns that pane.
 * Each component is given the specified X-alignment.
 */
	public static final JPanel wrapInFLPaneAndVBox(
		JComponent[] cmpnnts, float alignx) {

		return wrapInFLPaneAndVBox(cmpnnts, 0, alignx);
	}

/** Wraps the given list of components in a vertical box, wraps the box
 * in a flow layout pane, and returns that pane.
 * The box has a gap at the top of the specified size (which is expected
 * to be non-negative), and each component is given the default
 * X-alignment of 0.5 (which centres it in the box).
 */
	public static final JPanel wrapInFLPaneAndVBox(
		JComponent[] cmpnnts, int gap) {

		return wrapInFLPaneAndVBox(cmpnnts, gap, 0.5f);
	}

/** Wraps the given list of components in a vertical box, wraps the box
 * in a flow layout pane, and returns that pane.
 * The box has a gap at the top of the specified size (which is expected
 * to be non-negative), and each component is given the specified
 * X-alignment.
 */
	public static final JPanel wrapInFLPaneAndVBox(
		JComponent[] cmpnnts, int gap, float alignx) {
	
		return wrapInFlowLayoutPane(wrapInVerticalBox(cmpnnts, gap, alignx));
	}

/** Creates an returns a {@code Box} containing the given components,
 * giving each component the specified X-alignment.
 */
	public static final Box wrapInVerticalBox(
		JComponent[] cmpnnts, float alignx) {

		return wrapInVerticalBox(cmpnnts, 0, alignx);
	}

/** Creates an returns a {@code Box} containing the given components,
 * with a gap at the top of the specified size (which is expected to be
 * non-negative), and giving each component the defaul X-alignment
 * of 0.5 (which centres it within the box).
 */
	public static final Box wrapInVerticalBox(
		JComponent[] cmpnnts, int gap) {

		return wrapInVerticalBox(cmpnnts, gap, 0.5f);
	}

/** Creates an returns a {@code Box} containing the given components,
 * with a gap at the top of the specified size (which is expected to be
 * non-negative), and giving each component the specified X-alignment.
 */
	public static final Box wrapInVerticalBox(
		JComponent[] cmpnnts, int gap, float alignx) {

		Box box = Box.createVerticalBox();

		if (0 < gap)
			box.add(Box.createVerticalStrut(gap));

		for (JComponent cmpnnt : cmpnnts) {
			box.add(cmpnnt);
			cmpnnt.setAlignmentX(alignx);
		}

		return box;
	}

/** Wraps the given component in a {@code JPanel} and returns that
 * panel -- which has a {@code FlowLayout}, centred with no padding.
 */
	public static final JPanel wrapInFlowLayoutPane(Component cmpnnt) {

		return wrapInFlowLayoutPane(cmpnnt, FlowLayout.CENTER);
	}

/** Wraps the given component in a {@code JPanel} and returns that
 * panel -- which has a {@code FlowLayout} with the given alignment and
 * no padding.
 */
	public static final JPanel wrapInFlowLayoutPane(
		Component cmpnnt, int align) {

		JPanel pane = new JPanel(new FlowLayout(align, 0, 0));
		pane.add(cmpnnt);

		return pane;
	}

/** Wraps the given components in a {@code JPanel} and returns that
 * panel -- which has a {@code FlowLayout}, centred with no padding.
 */
	public static final JPanel wrapInFlowLayoutPane(Component[] cmpnnts) {

		JPanel pane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		for (Component cpt : cmpnnts)
			pane.add(cpt);

		return pane;
	}

/** Returns a new {@code JPanel} with a {@code BorderLayout} containing
 * the given pair of components in its north and south regions.  If either
 * component is {@code null} the corresponding region is empty.
 */
	public static final JPanel wrapInNCSPane(
		Component cnorth, Component ccentre, Component csouth) {

		return wrapInNWCESPane(cnorth, null, ccentre, null, csouth);
	}

/** Returns a new {@code JPanel} with a {@code BorderLayout} containing
 * the given components in its west, central and east regions.  If any
 * of these components is {@code null} the corresponding region is empty.
 */
	public static final JPanel wrapInWCEPane(
		Component cwest, Component ccentre, Component ceast) {

		return wrapInNWCESPane(null, cwest, ccentre, ceast, null);
	}

/** Returns a new {@code JPanel} with a {@code BorderLayout} containing
 * the given components in its north, west, centre, east and south
 * regions.  A {@code null} component value causes the corresponding
 * region to be empty.
 */
	public static final JPanel wrapInNWCESPane(
		Component cnorth,
		Component cwest, Component ccentre, Component ceast,
		Component csouth) {

		JPanel pane = new JPanel(new BorderLayout());

		if (cnorth != null)
			pane.add(cnorth, BorderLayout.NORTH);

		if (cwest != null)
			pane.add(cwest, BorderLayout.WEST);

		if (ccentre != null)
			pane.add(ccentre, BorderLayout.CENTER);

		if (ceast != null)
			pane.add(ceast, BorderLayout.EAST);

		if (csouth != null)
			pane.add(csouth, BorderLayout.SOUTH);

		return pane;
	}
}
