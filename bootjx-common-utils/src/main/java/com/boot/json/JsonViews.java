package com.boot.json;

public class JsonViews {
	public static class External {

	}

	public static class Public extends External {
	}

	public static class Internal extends Public {
	}

	/**
	 * Field will be visible in [TinyView,SummaryView,FullView] View
	 * 
	 * All the Fields with [TinyView,None] are going to be visible
	 * 
	 * @author lalittanwar
	 *
	 */
	public static interface Tiny {
	}

	/**
	 * Field will be visible in [TinyView,SummaryView,FullView] View
	 * 
	 * All the Fields with [TinyView,None] are going to be visible
	 * 
	 * @author lalittanwar
	 *
	 */
	public static interface Compact extends Tiny {
	}

	/**
	 * Field will be visible in [SummaryView,FullView] View.
	 * 
	 * All the Fields with [SummaryView,TinyView,None] are going to be visible
	 * 
	 * @author lalittanwar
	 *
	 */
	public static interface Summary extends Compact {
	}

	/**
	 * Field will be visible only in [FullView] View.
	 * 
	 * All the Fields with [FullView,SummaryView,TinyView,None] are going to be
	 * visible
	 * 
	 * @author lalittanwar
	 *
	 */
	public static interface Full extends Summary {
	}

}
