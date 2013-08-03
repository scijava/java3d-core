/*
 * Copyright 2005-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 */

package javax.media.j3d;

import java.util.ArrayList;
import java.util.Vector;

import javax.vecmath.Point3d;
import javax.vecmath.Point4d;

/**
 * The PickInfo object contains the computed information about a pick hit.
 * The detailed information about each intersection of the PickShape
 * with the picked Node can be inquired.  The PickInfo object is constructed with
 * basic information and more detailed information can be generated by setting the
 * appropriate mask to the flag argument in the pick methods of BranchGroup and
 * Locale.
 * <p>
 *
 * @see Locale
 * @see BranchGroup
 *
 * @since Java 3D 1.4
 */


public class PickInfo extends Object {

    static final int PICK_ALL = 1;

    static final int PICK_ANY = 2;

    /* The SceneGraphPath of the intersected pickable item */
    private SceneGraphPath sgp;

    /* The intersected pickable node object */
    private  Node node;

    /* A copy of LocalToVworld transform of the pickable node */
    private Transform3D l2vw;

    /* The closest intersection point */
    private Point3d closestIntersectionPoint;

    /* Distance between start point of pickShape and closest intersection point */
    private double  closestDistance;

    /* An array to store intersection results */
    private IntersectionInfo[] intersectionInfoArr;

    /* The following references are for internal geometry computation use only */
    private ArrayList<IntersectionInfo> intersectionInfoList = new ArrayList<IntersectionInfo>();
    private boolean intersectionInfoListSorted = false;
    private Transform3D l2vwRef;
    private Node nodeRef;

    /**
     * Specifies a Pick using the bounds of the pickable nodes.
     */
    public static final int PICK_BOUNDS = 1;

    /**
     * Specifies a Pick using the geometry of the pickable nodes.
     */
    public static final int PICK_GEOMETRY = 2;

    /**
   * Specifies that this PickInfo returns the computed SceneGraphPath object.
   */
    public static final int SCENEGRAPHPATH  = 0x01;

    /**
     * Specifies that this PickInfo returns the computed intersected Node object.
     */
    public static final int NODE = 0x02;

    /**
     * Specifies that this PickInfo returns the computed local to vworld transform.
     */
    public static final int LOCAL_TO_VWORLD = 0x04;

    /**
     * Specifies that this PickInfo returns the closest intersection point.
     */
    public static final int CLOSEST_INTERSECTION_POINT = 0x08;

    /**
     * Specifies that this PickInfo returns the closest intersection distance.
     */
    public static final int CLOSEST_DISTANCE = 0x10;

    /**
     * Specifies that this PickInfo returns only the closest intersection
     * geometry information.
     */
    public static final int CLOSEST_GEOM_INFO = 0x20;

    /**
     * Specifies that this PickInfo returns all the closest intersection
     * geometry informations.
     */
    public static final int ALL_GEOM_INFO = 0x40;


    /** PickInfo Constructor */
    PickInfo() {

    }

    void setSceneGraphPath(SceneGraphPath sgp) {
        this.sgp = sgp;
    }

    void setNode(Node node) {
        this.node = node;
    }

    void setLocalToVWorld(Transform3D l2vw) {
        this.l2vw = l2vw;
    }

    void setClosestIntersectionPoint(Point3d cIPt) {
        this.closestIntersectionPoint = cIPt;
    }

    void setClosestDistance(double cDist) {
        this.closestDistance = cDist;
    }

    void setLocalToVWorldRef(Transform3D l2vwRef) {
        this.l2vwRef = l2vwRef;
    }

    void setNodeRef(Node nodeRef) {
        this.nodeRef = nodeRef;
    }

    IntersectionInfo createIntersectionInfo() {
        return new IntersectionInfo();
    }

    void insertIntersectionInfo(IntersectionInfo iInfo) {
        intersectionInfoList.add(iInfo);
        intersectionInfoListSorted = false;
    }

    void sortIntersectionInfoArray(IntersectionInfo[] iInfoArr) {

        class Sort {

	    IntersectionInfo iInfoArr[];

	    Sort(IntersectionInfo[] iInfoArr) {
                // System.err.println("Sort IntersectionInfo ...");
		this.iInfoArr = iInfoArr;
	    }

	    void sorting() {
		if (iInfoArr.length < 7) {
                    // System.err.println(" -- insertSort.");
		    insertSort();
	    	} else {
                    // System.err.println(" -- quicksort.");
		    quicksort(0, iInfoArr.length-1);
    		}
	    }

	    // Insertion sort on smallest arrays
	    final void insertSort() {
		for (int i=0; i<iInfoArr.length; i++) {
		    for (int j=i; j>0 &&
                             (iInfoArr[j-1].distance > iInfoArr[j].distance); j--) {
			IntersectionInfo iInfo = iInfoArr[j];
			iInfoArr[j] = iInfoArr[j-1];
			iInfoArr[j-1] = iInfo;
		    }
		}
	    }

            final void quicksort( int l, int r ) {
		int i = l;
		int j = r;
		double k = iInfoArr[(l+r) / 2].distance;

		do {
		    while (iInfoArr[i].distance<k) i++;
		    while (k<iInfoArr[j].distance) j--;
		    if (i<=j) {
			IntersectionInfo iInfo = iInfoArr[i];
			iInfoArr[i] = iInfoArr[j];
			iInfoArr[j] = iInfo;
			i++;
			j--;
		    }
		} while (i<=j);

		if (l<j) quicksort(l,j);
		if (l<r) quicksort(i,r);
	    }
	}

	(new Sort(iInfoArr)).sorting();
        intersectionInfoListSorted = true;
    }

    static void sortPickInfoArray(PickInfo[] pickInfoArr) {

        class Sort {

	    PickInfo pIArr[];

	    Sort(PickInfo[] pIArr) {
                // System.err.println("Sort PickInfo ...");
		this.pIArr = pIArr;
	    }

	    void sorting() {
		if (pIArr.length < 7) {
                    // System.err.println(" -- insertSort.");
		    insertSort();
	    	} else {
                    // System.err.println(" -- quicksort.");
		    quicksort(0, pIArr.length-1);
    		}
	    }

	    // Insertion sort on smallest arrays
	    final void insertSort() {
		for (int i=0; i<pIArr.length; i++) {
		    for (int j=i; j>0 &&
                             (pIArr[j-1].closestDistance > pIArr[j].closestDistance); j--) {
			PickInfo pI = pIArr[j];
			pIArr[j] = pIArr[j-1];
			pIArr[j-1] = pI;
		    }
		}
	    }

            final void quicksort( int l, int r ) {
		int i = l;
		int j = r;
		double k = pIArr[(l+r) / 2].closestDistance;

		do {
		    while (pIArr[i].closestDistance<k) i++;
		    while (k<pIArr[j].closestDistance) j--;
		    if (i<=j) {
			PickInfo pI = pIArr[i];
			pIArr[i] = pIArr[j];
			pIArr[j] = pI;
			i++;
			j--;
		    }
		} while (i<=j);

		if (l<j) quicksort(l,j);
		if (l<r) quicksort(i,r);
	    }
	}

	(new Sort(pickInfoArr)).sorting();

    }


    /**
     * Retrieves the reference to the SceneGraphPath in this PickInfo object.
     * @return the SceneGraphPath object, or null if  flag is not set with SCENEGRAPHPATH.
     * @see Locale
     * @see BranchGroup
     */
    public SceneGraphPath getSceneGraphPath() {
	return sgp;
    }

    /**
     * Retrieves the reference to the picked node, either a Shape3D or a Morph, in this PickInfo object.
     * @return the picked leaf node object, or null if  flag is not set with NODE.
     * @see Locale
     * @see BranchGroup
     */
    public Node getNode() {
	return node;
    }

    /**
     * Retrieves the reference to the LocalToVworld transform of the picked node in this PickInfo object.
     * @return the local to vworld transform, or null if  flag is not set with LOCAL_TO_VWORLD.
     * @see Locale
     * @see BranchGroup
     */
    public Transform3D getLocalToVWorld() {
	return l2vw;
    }

    /**
     * Retrieves the reference to the closest intersection point in this PickInfo object.
     * @return the closest intersection point, or null if  flag is not set with CLOSEST_INTERSECTION_POINT.
     * @see Locale
     * @see BranchGroup
     */
    public Point3d getClosestIntersectionPoint() {
	return closestIntersectionPoint;
    }

    /**
     * Retrieves the distance between the start point of the pickShape and the closest intersection point.
     * @return the closest distance in double, or NaN if  flag is not set with CLOSEST_INTERSECTION_POINT.
     * Note : If this PickInfo object is returned by either pickClosest or pickAllSorted method, the return
     * value is the closest distance in double even if flag is not set with CLOSET_INTERSECTION_POINT.
     * @see Locale
     * @see BranchGroup
     */
    public double getClosestDistance() {
	return closestDistance;
    }

    Transform3D getLocalToVWorldRef() {
        return l2vwRef;
    }

    Node getNodeRef() {
        return nodeRef;
    }

    /**
     * Retrieves the reference to the array of intersection results in this PickInfo object.
     * @return an array of 1 IntersectionInfo object if flag is to set  CLOSEST_GEOM_INFO,
     * or an array of <i>N</i> IntersectionInfo objects containing all intersections of
     * the picked node in sorted order if flag is to set ALL_GEOM_INFO, or null if neither
     * bit is set.
     * @see Locale
     * @see BranchGroup
     */
    public IntersectionInfo[] getIntersectionInfos() {
        if (intersectionInfoListSorted == false) {
            intersectionInfoArr = new IntersectionInfo[intersectionInfoList.size()];
            intersectionInfoArr = intersectionInfoList.toArray(intersectionInfoArr);

            sortIntersectionInfoArray(intersectionInfoArr);
         }

        return intersectionInfoArr;
    }

/**
 * Search the path from nodeR up to Locale.
 * Return the search path as ArrayList if found.
 * Note that the locale will not insert into path.
 */
static ArrayList<NodeRetained> initSceneGraphPath(NodeRetained nodeR) {
	ArrayList<NodeRetained> path = new ArrayList<NodeRetained>(5);

	do {
		if (nodeR.source.getCapability(Node.ENABLE_PICK_REPORTING)) {
			path.add(nodeR);
		}
		nodeR = nodeR.parent;
	} while (nodeR != null); // reach Locale

	return path;
}

    static private Node[] createPath(NodeRetained srcNode,
				     BranchGroupRetained bgRetained,
				     GeometryAtom geomAtom,
				     ArrayList<NodeRetained> initpath) {

        ArrayList<NodeRetained> path = retrievePath(srcNode, bgRetained,
				      geomAtom.source.key);
        assert(path != null);

        return mergePath(path, initpath);

    }


    /**
     * Return true if bg is inside cachedBG or bg is null
     */
    static private boolean inside(BranchGroupRetained bgArr[],
				  BranchGroupRetained bg) {

	if ((bg == null) || (bgArr == null)) {
	    return true;
	}

	for (int i=0; i < bgArr.length; i++) {
	    if (bgArr[i] == bg) {
		return true;
	    }
	}
	return false;
    }

    /**
     * search the full path from the bottom of the scene graph -
     * startNode, up to the Locale if endNode is null.
     * If endNode is not null, the path is found up to, but not
     * including, endNode or return null if endNode not hit
     * during the search.
     */
    static private ArrayList<NodeRetained> retrievePath(NodeRetained startNode,
					  NodeRetained endNode,
					  HashKey key) {

	ArrayList<NodeRetained> path = new ArrayList<NodeRetained>(5);
	NodeRetained nodeR = startNode;

	if (nodeR.inSharedGroup) {
	    // getlastNodeId() will destroy this key
	    key = new HashKey(key);
	}

	do {
	    if (nodeR == endNode) { // we found it !
		return path;
	    }

	    if (nodeR.source.getCapability(Node.ENABLE_PICK_REPORTING)) {
		path.add(nodeR);
	    }

	    if (nodeR instanceof SharedGroupRetained) {
		// retrieve the last node ID
		String nodeId = key.getLastNodeId();
		Vector<NodeRetained> parents = ((SharedGroupRetained)nodeR).parents;
		int sz = parents.size();
		NodeRetained prevNodeR = nodeR;
		for(int i=0; i< sz; i++) {
			NodeRetained linkR = parents.get(i);
		    if (linkR.nodeId.equals(nodeId)) {
			nodeR = linkR;
			// Need to add Link to the path report
			path.add(nodeR);
			// since !(endNode instanceof Link), we
			// can skip the check (nodeR == endNode) and
			// proceed to parent of link below
			break;
		    }
		}
		if (nodeR == prevNodeR) {
		    // branch is already detach
		    return null;
		}
	    }
	    nodeR = nodeR.parent;
	} while (nodeR != null); // reach Locale

	if (endNode == null) {
	    // user call pickxxx(Locale locale, PickShape shape)
	    return path;
	}

	// user call pickxxx(BranchGroup endNode, PickShape shape)
	// if locale is reached and endNode not hit, this is not
	// the path user want to select
	return null;
    }

    /**
     * copy p1, (follow by) p2 into a new array, p2 can be null
     * The path is then reverse before return.
     */
    static private Node[] mergePath(ArrayList<NodeRetained> p1, ArrayList<NodeRetained> p2) {
	int s = p1.size();
	int len;
	int i;
	int l;
	if (p2 == null) {
	    len = s;
	} else {
	    len = s + p2.size();
	}

	Node nodes[] = new Node[len];
	l = len-1;
	for (i=0; i < s; i++) {
	    nodes[l-i] = (Node)p1.get(i).source;
	}
	for (int j=0; i< len; i++, j++) {
	    nodes[l-i] = (Node)p2.get(j).source;
	}
	return nodes;
    }

    /**
     * Sort the GeometryAtoms distance from shape in ascending order
     * geomAtoms.length must be >= 1
     */
    static void sortGeomAtoms(GeometryAtom geomAtoms[],
				      PickShape shape) {

	final double distance[] = new double[geomAtoms.length];
	Point4d pickPos = new Point4d();

	for (int i=0; i < geomAtoms.length; i++) {
	    shape.intersect(geomAtoms[i].source.vwcBounds, pickPos);
	    distance[i] = pickPos.w;
	}

	class Sort {

	    GeometryAtom atoms[];

	    Sort(GeometryAtom[] atoms) {
		this.atoms = atoms;
	    }

	    void sorting() {
		if (atoms.length < 7) {
		    insertSort();
	    	} else {
		    quicksort(0, atoms.length-1);
    		}
	    }

	    // Insertion sort on smallest arrays
	    final void insertSort() {
		for (int i=0; i<atoms.length; i++) {
		    for (int j=i; j>0 &&
			     (distance[j-1] > distance[j]); j--) {
			double t = distance[j];
			distance[j] = distance[j-1];
			distance[j-1] = t;
			GeometryAtom p = atoms[j];
			atoms[j] = atoms[j-1];
			atoms[j-1] = p;
		    }
		}
	    }

            final void quicksort( int l, int r ) {
		int i = l;
		int j = r;
		double k = distance[(l+r) / 2];

		do {
		    while (distance[i]<k) i++;
		    while (k<distance[j]) j--;
		    if (i<=j) {
			double tmp = distance[i];
			distance[i] =distance[j];
			distance[j] = tmp;

			GeometryAtom p=atoms[i];
			atoms[i]=atoms[j];
			atoms[j]=p;
			i++;
			j--;
		    }
		} while (i<=j);

		if (l<j) quicksort(l,j);
		if (l<r) quicksort(i,r);
	    }
	}

	(new Sort(geomAtoms)).sorting();
    }


    /**
     * return all PickInfo[] of the geomAtoms.
     * If initpath is null, the path is search from
     * geomAtom Shape3D/Morph Node up to Locale
     * (assume the same locale).
     * Otherwise, the path is search up to node or
     * null is return if it is not hit.
     */
    static ArrayList<PickInfo> getPickInfos(ArrayList<NodeRetained> initpath,
                                  BranchGroupRetained bgRetained,
				  GeometryAtom geomAtoms[],
			          Locale locale, int flags, int pickType) {

        ArrayList<PickInfo> pickInfoList = new ArrayList<PickInfo>(5);
        NodeRetained srcNode;
        ArrayList text3dList = null;

        if ((geomAtoms == null) || (geomAtoms.length == 0)) {
            return null;
        }

	for (int i=0; i < geomAtoms.length; i++) {
            assert((geomAtoms[i] != null) &&
                    (geomAtoms[i].source != null));

	    PickInfo pickInfo = null;
            Shape3DRetained shape = geomAtoms[i].source;
            srcNode = shape.sourceNode;

            // Fix to Issue 274 : NPE With Simultaneous View and Content Side PickingBehaviors
            // This node isn't under the selected BG for pick operation.
            if (!inside(shape.branchGroupPath,bgRetained)) {
                continue;
            }

            if (srcNode == null) {
                // The node is just detach from branch so sourceNode = null
                continue;
            }


            // Special case, for Text3DRetained, it is possible
            // for different geomAtoms pointing to the same
            // source Text3DRetained. So we need to combine
            // those cases and report only once.
            if (srcNode instanceof Shape3DRetained) {
                Shape3DRetained s3dR = (Shape3DRetained) srcNode;
                GeometryRetained geomR = null;
                for(int cnt=0; cnt<s3dR.geometryList.size(); cnt++) {
				geomR = s3dR.geometryList.get(cnt);
                    if(geomR != null)
                        break;
                }

                if (geomR == null)
                    continue;

                if (geomR instanceof Text3DRetained) {
                    // assume this case is not frequent, we allocate
                    // ArrayList only when necessary and we use ArrayList
                    // instead of HashMap since the case of when large
                    // number of distingish Text3DRetained node hit is
                    // rare.
                    if (text3dList == null) {
                        text3dList = new ArrayList(3);
                    } else {
                        int size = text3dList.size();
                        boolean found = false;
                        for (int j=0; j < size; j++) {
                            if (text3dList.get(j) == srcNode) {
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            continue;  // try next geomAtom
                        }
                    }
                    text3dList.add(srcNode);
                }
            }

            // If srcNode is instance of compile retained, then loop thru
            // the entire source list and add it to the scene graph path
            if (srcNode instanceof Shape3DCompileRetained) {

                Shape3DCompileRetained s3dCR = (Shape3DCompileRetained)srcNode;

                Node[] mpath = null;
                boolean first = true;

                for (int n = 0; n < s3dCR.srcList.length; n++) {

                    pickInfo = null;

                    // PickInfo.SCENEGRAPHPATH - request for computed SceneGraphPath.
                    if ((flags & SCENEGRAPHPATH) != 0){

                        if(first) {
                            mpath = createPath(srcNode, bgRetained, geomAtoms[i], initpath);
                            first = false;
                        }

                        if(mpath != null) {
                            SceneGraphPath sgpath = new SceneGraphPath(locale,
                                    mpath, (Node) s3dCR.srcList[n]);
                            sgpath.setTransform(shape.getCurrentLocalToVworld(0));
			    if(pickInfo == null)
				pickInfo = new PickInfo();
                            pickInfo.setSceneGraphPath(sgpath);
                        }
                    }

                    // PickInfo.NODE - request for computed intersected Node.
                    if ((flags & NODE) != 0) {
			if(pickInfo == null)
			    pickInfo = new PickInfo();
                        pickInfo.setNode((Node) s3dCR.srcList[n]);
                    }

                    // PickInfo.LOCAL_TO_VWORLD
                    //    - request for computed local to virtual world transform.
                    if ((flags & LOCAL_TO_VWORLD) != 0) {
                        Transform3D l2vw = geomAtoms[i].source.getCurrentLocalToVworld();
			if(pickInfo == null)
			    pickInfo = new PickInfo();
                        pickInfo.setLocalToVWorld( new Transform3D(l2vw));
                    }

                    // NOTE : Piggy bag for geometry computation by caller.
                    if (((flags & CLOSEST_DISTANCE) != 0) ||
                        ((flags & CLOSEST_GEOM_INFO) != 0) ||
                        ((flags & CLOSEST_INTERSECTION_POINT) != 0) ||
                        ((flags & ALL_GEOM_INFO) != 0)) {
			if(pickInfo == null)
			    pickInfo = new PickInfo();
                        pickInfo.setNodeRef((Node) s3dCR.srcList[n]);
                        Transform3D l2vw = geomAtoms[i].source.getCurrentLocalToVworld();
			pickInfo.setLocalToVWorldRef(l2vw);
                    }

		    if(pickInfo != null)
			pickInfoList.add(pickInfo);
                    if(pickType == PICK_ANY) {
                        return pickInfoList;
                    }
                }
            }
            else {
                Node[] mpath = null;

                // PickInfo.SCENEGRAPHPATH - request for computed SceneGraphPath.
                if ((flags & SCENEGRAPHPATH) != 0) {

                    mpath = createPath(srcNode, bgRetained, geomAtoms[i], initpath);

                    if(mpath != null) {
                        SceneGraphPath sgpath = new SceneGraphPath(locale, mpath,
                                (Node) srcNode.source);
                        sgpath.setTransform(shape.getCurrentLocalToVworld(0));
                        if(pickInfo == null)
                            pickInfo = new PickInfo();
                        pickInfo.setSceneGraphPath(sgpath);
                    }
                }

                // PickInfo.NODE - request for computed intersected Node.
                if ((flags & NODE) != 0) {
		    if(pickInfo == null)
			pickInfo = new PickInfo();
                    pickInfo.setNode((Node) srcNode.source);
                }

                // PickInfo.LOCAL_TO_VWORLD
                //    - request for computed local to virtual world transform.
                if ((flags & LOCAL_TO_VWORLD) != 0) {
                    Transform3D l2vw = geomAtoms[i].source.getCurrentLocalToVworld();
		    if(pickInfo == null)
			pickInfo = new PickInfo();
                    pickInfo.setLocalToVWorld( new Transform3D(l2vw));
                }

                // NOTE : Piggy bag for geometry computation by caller.
                if (((flags & CLOSEST_DISTANCE) != 0) ||
                    ((flags & CLOSEST_GEOM_INFO) != 0) ||
                    ((flags & CLOSEST_INTERSECTION_POINT) != 0) ||
                    ((flags & ALL_GEOM_INFO) != 0)) {
		    if(pickInfo == null)
			pickInfo = new PickInfo();
                    pickInfo.setNodeRef((Node) srcNode.source);
                    Transform3D l2vw = geomAtoms[i].source.getCurrentLocalToVworld();
                    pickInfo.setLocalToVWorldRef(l2vw);
                }

		if(pickInfo != null)
		    pickInfoList.add(pickInfo);
                if(pickType == PICK_ANY) {
                    return pickInfoList;
                }
            }
        }

	return pickInfoList;
    }

    static PickInfo[] pick(Object node, GeometryAtom[] geomAtoms,
            int mode, int flags, PickShape pickShape, int pickType) {

        int pickInfoListSize;
        PickInfo[] pickInfoArr = null;
        Locale locale = null;
        BranchGroupRetained bgRetained = null;
        ArrayList<PickInfo> pickInfoList = null;

        if (node instanceof Locale) {
            locale = (Locale) node;
        }
        else if ( node instanceof BranchGroupRetained) {
            bgRetained = (BranchGroupRetained) node;
            locale = bgRetained.locale;
        }
        synchronized (locale.universe.sceneGraphLock) {
            ArrayList<NodeRetained> initPath = null;
            if ( bgRetained != null) {
                initPath = initSceneGraphPath(bgRetained);
            }
            pickInfoList = getPickInfos(initPath, bgRetained, geomAtoms,
                locale, flags, pickType);
        }

        // We're done with PICK_BOUNDS case, but there is still more work for PICK_GEOMETRY case.
        if((mode == PICK_GEOMETRY) && (pickInfoList != null) &&
	   ((pickInfoListSize = pickInfoList.size()) > 0)) {

            //System.err.println("PickInfo.pick() - In geometry case : pickInfoList.size() is " + pickInfoListSize);
            Node pickNode = null;

            // Order is impt. Need to do in reverse order.
            for(int i = pickInfoListSize - 1; i >= 0; i--) {
            	PickInfo pickInfo = pickInfoList.get(i);

                pickNode = pickInfo.getNode();
                if( pickNode == null) {
                    // Use the piggy reference from getPickInfos()
                    pickNode = pickInfo.getNodeRef();
                }

                if (pickNode instanceof Shape3D) {

		    /*
		     * @exception CapabilityNotSetException if the mode is
		     * PICK_GEOMETRY and the Geometry.ALLOW_INTERSECT capability bit
		     * is not set in any Geometry objects referred to by any shape
		     * node whose bounds intersects the PickShape.
		     *
                     * @exception CapabilityNotSetException if flags contains any of
		     * CLOSEST_INTERSECTION_POINT, CLOSEST_DISTANCE, CLOSEST_GEOM_INFO
		     * or ALL_GEOM_INFO, and the capability bits that control reading of
		     * coordinate data are not set in any GeometryArray object referred
		     * to by any shape node that intersects the PickShape.
		     * The capability bits that must be set to avoid this exception are
                     * as follows :
		     *
		     * By-copy geometry : GeometryArray.ALLOW_COORDINATE_READ
		     * By-reference geometry : GeometryArray.ALLOW_REF_DATA_READ
		     * Indexed geometry : IndexedGeometryArray.ALLOW_COORDINATE_INDEX_READ
		     * (in addition to one of the above)
		     *
		     */

                    if (!pickNode.getCapability(Shape3D.ALLOW_GEOMETRY_READ)) {
			throw new CapabilityNotSetException(J3dI18N.getString("PickInfo0"));
		    }

		    for (int j = 0; j < ((Shape3D)pickNode).numGeometries(); j++) {
			Geometry geo = ((Shape3D)pickNode).getGeometry(j);

			if(geo == null) {
			    continue;
			}

			if(!geo.getCapability(Geometry.ALLOW_INTERSECT)) {
			    throw new CapabilityNotSetException(J3dI18N.getString("PickInfo1"));
			}

			if (geo instanceof GeometryArray) {
			    if(!geo.getCapability(GeometryArray.ALLOW_COORDINATE_READ))
				throw new CapabilityNotSetException(J3dI18N.getString("PickInfo2"));
			    if(!geo.getCapability(GeometryArray.ALLOW_COUNT_READ))
				throw new CapabilityNotSetException(J3dI18N.getString("PickInfo3"));
			    if(!geo.getCapability(GeometryArray.ALLOW_FORMAT_READ))
				throw new CapabilityNotSetException(J3dI18N.getString("PickInfo4"));
			    if (geo instanceof IndexedGeometryArray) {
				if(!geo.getCapability(IndexedGeometryArray.ALLOW_COORDINATE_INDEX_READ))
				    throw new CapabilityNotSetException(J3dI18N.getString("PickInfo5"));
			    }
			} else if (geo instanceof CompressedGeometry) {
			    if(!geo.getCapability(CompressedGeometry.ALLOW_GEOMETRY_READ))
				throw new CapabilityNotSetException(J3dI18N.getString("PickInfo0"));
			}
		    }

		    if (((Shape3DRetained)(pickNode.retained)).intersect(pickInfo, pickShape, flags) == false) {
			// System.err.println("  ---- geom " + i + " not intersected");

                        pickInfoList.remove(i);

                    }
                    else if(pickType == PICK_ANY) {
                        pickInfoArr = new PickInfo[1];
                        pickInfoArr[0] = pickInfo;
                        return pickInfoArr;
                    }
                } else if (pickNode instanceof Morph) {

		    /*
		     * @exception CapabilityNotSetException if the mode is
		     * PICK_GEOMETRY and the Geometry.ALLOW_INTERSECT capability bit
		     * is not set in any Geometry objects referred to by any shape
		     * node whose bounds intersects the PickShape.
		     *
                     * @exception CapabilityNotSetException if flags contains any of
		     * CLOSEST_INTERSECTION_POINT, CLOSEST_DISTANCE, CLOSEST_GEOM_INFO
		     * or ALL_GEOM_INFO, and the capability bits that control reading of
		     * coordinate data are not set in any GeometryArray object referred
		     * to by any shape node that intersects the PickShape.
		     * The capability bits that must be set to avoid this exception are
                     * as follows :
		     *
		     * By-copy geometry : GeometryArray.ALLOW_COORDINATE_READ
		     * By-reference geometry : GeometryArray.ALLOW_REF_DATA_READ
		     * Indexed geometry : IndexedGeometryArray.ALLOW_COORDINATE_INDEX_READ
		     * (in addition to one of the above)
		     *
		     */

                    if (!pickNode.getCapability(Morph.ALLOW_GEOMETRY_ARRAY_READ)) {
			throw new CapabilityNotSetException(J3dI18N.getString("PickInfo6"));
		    }

		    int numGeo = ((MorphRetained)(pickNode.retained)).getNumGeometryArrays();
		    for (int j = 0; j < numGeo; j++) {
			GeometryArray geo = ((Morph)pickNode).getGeometryArray(j);

			if(geo == null) {
			    continue;
			}

			if(!geo.getCapability(Geometry.ALLOW_INTERSECT)) {
			    throw new CapabilityNotSetException(J3dI18N.getString("PickInfo1"));
			}

			if(!geo.getCapability(GeometryArray.ALLOW_COORDINATE_READ))
			    throw new CapabilityNotSetException(J3dI18N.getString("PickInfo2"));
			if(!geo.getCapability(GeometryArray.ALLOW_COUNT_READ))
			    throw new CapabilityNotSetException(J3dI18N.getString("PickInfo3"));
			if(!geo.getCapability(GeometryArray.ALLOW_FORMAT_READ))
			    throw new CapabilityNotSetException(J3dI18N.getString("PickInfo4"));

			if (geo instanceof IndexedGeometryArray) {
			    if(!geo.getCapability(IndexedGeometryArray.ALLOW_COORDINATE_INDEX_READ))
				throw new CapabilityNotSetException(J3dI18N.getString("PickInfo5"));
			}
		    }

                    if (((MorphRetained)(pickNode.retained)).intersect(pickInfo, pickShape, flags) == false) {
                        pickInfoList.remove(i);
                    }
                    else if(pickType == PICK_ANY) {
                        pickInfoArr = new PickInfo[1];
                        pickInfoArr[0] = pickInfo;
                        return pickInfoArr;
                    }
                }
            }
        }

	// System.err.println("PickInfo : pickInfoList " + pickInfoList);

        if ((pickInfoList != null) && (pickInfoList.size() > 0)) {
	    // System.err.println("   ---  : pickInfoList.size() " + pickInfoList.size());
	    // System.err.println("   ---  : pickInfoList's sgp " +
	    // ((PickInfo)(pickInfoList.get(0))).getSceneGraphPath());
	    pickInfoArr = new PickInfo[pickInfoList.size()];
	    return pickInfoList.toArray(pickInfoArr);
	}

	return null;

    }

    /**
     * The IntersectionInfo object holds extra information about an intersection
     * of a PickShape with a Node as part of a PickInfo. Information such as
     * the intersected geometry, the intersected point, and the vertex indices
     * can be inquired.
     * The local coordinates, normal, color and texture coordiantes of at the
     * intersection can be computed, if they are present and readable, using the
     * interpolation weights and vertex indices.
     * <p>
     * If the Shape3D being picked has multiple geometry arrays, the possible arrays
     * of IntersectionInfo are stored in the PickInfo and referred to by a geometry
     * index. If the picked geometry is of type, Text3D or CompressGeometry,
     * getVertexIndices is invalid. If the picked Node is an Morph
     * object, the geometry used in pick computation is alway at index 0.
     * <p>
     *
     * @since Java 3D 1.4
     */

    public class IntersectionInfo extends Object {

	/* The index to the intersected geometry in the pickable node */
	private int geomIndex;

        /* The reference to the intersected geometry in the pickable object */
	private Geometry geom;

	/* The intersection point */
	private Point3d intersectionPoint;

	/* Distance between start point of pickShape and intersection point */
	private double  distance;

	/* The vertex indices of the intersected primitive in the geometry */
	private int[] vertexIndices;

	/* The interpolation weights for each of the verticies of the primitive */
	// private float[] weights;  Not supported. Should be done in util. package

	/** IntersectionInfo Constructor */
	IntersectionInfo() {

	}

        void setGeometryIndex(int geomIndex) {
            this.geomIndex = geomIndex;
        }

        void setGeometry(Geometry geom) {
            this.geom = geom;
        }

        void setIntersectionPoint(Point3d intersectionPoint) {
	    assert(intersectionPoint != null);
	    this.intersectionPoint = new Point3d(intersectionPoint);
        }

        void setDistance(double distance) {
            this.distance = distance;
        }

        void setVertexIndices(int[] vertexIndices) {
	    assert(vertexIndices != null);
	    this.vertexIndices = new int[vertexIndices.length];
	    for(int i=0; i<vertexIndices.length; i++) {
		this.vertexIndices[i] = vertexIndices[i];
	    }
	}


	/**
	 * Retrieves the index to the intersected geometry in the picked node, either a Shape3D or Morph.
	 * @return the index of the intersected geometry in the pickable node.
	 */
	public int getGeometryIndex() {
	    return geomIndex;
	}

	/**
	 * Retrieves the reference to the intersected geometry in the picked object, either a Shape3D or Morph.
	 * @return the intersected geometry in the pickable node.
	 */
	public Geometry getGeometry() {
	    return geom;
	}

	/**
	 * Retrieves the reference to the intersection point in the pickable node.
	 * @return the intersected point in the pickable node.
	 */
	public Point3d getIntersectionPoint() {
	    return intersectionPoint;
	}

	/**
	 * Retrieves the distance between the start point of the pickShape and the
	 * intersection point.
	 * @return distance between the start point of the pickShape and the
	 * intersection point.
	 */
	public double getDistance() {
	    return distance;
	}

	/**
	 * Retrieves the vertex indices of the intersected primitive in the geometry.
	 * @return the vertex indices of the intersected primitive.
	 */
	public int[] getVertexIndices() {
	    return vertexIndices;
	}

    }
}


