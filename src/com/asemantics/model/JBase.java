package com.asemantics.model;

/**
 * Represents the base class to represent every code entity.
 *
 * @author Michele Mostarda (michele.mostarda@gmail.com)
 */
public abstract class JBase {

    /**
     * The observed code org.asemantics.model.
     */
    private QueryModel queryModel;

    /**
     * The parent container.
     */
    protected JContainer parent;

    /**
     * The name of the container.
     */
    private String name;

    /**
     * Construction by sections.
     * @param qm
     * @param sections
     */
    protected JBase(QueryModel qm, String[] sections) throws QueryModelException {
        if(qm == null) {
            throw new NullPointerException("qm cannot be null");
        }
        if(sections == null) {
            throw new NullPointerException("name cannot be null.");
        }
        if(sections.length == 0) {
            throw new IllegalArgumentException("sections length cannot be 0.");
        }

        queryModel = qm;
        HierarchyResult hr = makeHierarchy(qm, sections);
        parent    = hr.p;
        name      = hr.n;
    }

    /**
     * Constructions by path.
     * @param qm
     * @param path
     */
    public JBase(QueryModel qm, String path) throws QueryModelException {
        this( qm , splitPath(path) );
    }

    /**
     * Returns the relative name of the hierarchy element.
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the full name of the yerarchy element.
     * @return
     */
    public String getFullName() {
        return parent.getPathAsString() + CodeHandler.PACKAGE_SEPARATOR + name;
    }

    /**
     * Returns the associated query org.asemantics.model.
     * @return
     */
    public QueryModel getQueryModel() {
        return queryModel;
    }

    /**
     * Returns the element parent if any.
     * @return
     */
    public JContainer getParent() {
        return parent;
    }

    /**
     * Check the existence of an entity named with 'name[0]. ... name[index]'.
     * @param name
     * @return
     */
    public abstract boolean exists(final String[] name, int index);

    /**
     * Return the hierarchy element type.
     * @return
     */
    protected abstract String getHyerarchyElemType();

    /**
     * Validates the sections value.
     * @param sections
     */
    protected final void validateAndCheckName(final String[] sections, int index)
    throws QueryModelException {
        if(sections == null) {
            throw new NullPointerException("sections cannot be null.");
        }
        if(sections.length == 0) {
            throw  new IllegalArgumentException("sections length must be > 0");
        }

        if( ! Character.isJavaIdentifierStart( sections[index].charAt(0) ) ) {
            throw new IllegalArgumentException("Invalid char into section '" + sections[index] + "' at position 0.");
        }
        for(int i = 1; i < sections[index].length(); i++) {
            if( ! Character.isJavaIdentifierPart( sections[index].charAt(i) ) ) {
                throw new IllegalArgumentException("Invalid char into section '" + sections[index] + "' at position " + i + ".");
            }
        }
        if( ! exists(sections, index) ) {
            throw new QueryModelException("entity '" + concatenate(sections, index) + "' doesn't exist.");
        }
    }

    /**
     * Concatenate the sections from 0 to index in a string package separated.
     * @param sections
     * @param index
     * @return
     */
    public static final String concatenate(final String[] sections, final int index) {
        StringBuffer sb = new StringBuffer();
        for(int s = 0; s <= index; s++) {
            sb.append(sections[s]);
            if(s != index) {
                sb.append(CodeHandler.PACKAGE_SEPARATOR);
            }
        }
        return sb.toString();
    }


    /**
     * Splits the container path into sections.
     * @param path
     * @return
     */
    public static final String[] splitPath(String path) {
        int psi = path.indexOf(CodeModel.PREFIX_SEPARATOR); // Ignoring prefix identifier if any.
        String subPath = path.substring(psi + 1);
        String[] sectionNames = subPath.split("\\" + CodeHandler.PACKAGE_SEPARATOR);
        return sectionNames;
    }

    /**
     * Contains a hierarchy result.
     */
    private class HierarchyResult {
        JContainer p;
        String n;
    }

    /**
     * Creates a hierarchy result on given org.asemantics.model and sections.
     * @param qm
     * @param sections
     * @return
     */
    protected final HierarchyResult makeHierarchy(QueryModel qm, String[] sections)
    throws QueryModelException {

        JBase parent = null;
        validateAndCheckName(sections, sections.length -1);
        if(sections.length > 1) {
            String parentType  = qm.getRDFType( concatenate(sections, sections.length - 2) );
            String[] parentSections = new String[sections.length -1];
            System.arraycopy(sections, 0, parentSections, 0, sections.length -1);
            parent = CoderFactory.createBaseOnRDFClass(qm, parentSections, parentType);
        }
        HierarchyResult hr = new HierarchyResult();
        hr.p = (JContainer) parent;
        hr.n = sections[sections.length - 1];
        return hr;
    }

    public String toString() {
        return  getParent().getPathAsString() + CodeHandler.PACKAGE_SEPARATOR +  getName() + ":" + getHyerarchyElemType();
    }

}
