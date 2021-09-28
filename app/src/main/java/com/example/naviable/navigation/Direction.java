package com.example.naviable.navigation;

public class Direction {
	private String description;
	private String type;

	/**
	 * base on the python file in assets:
	 * <p>
	 * 'straight',
	 * 'right',
	 * 'left',
	 * 'ramp',
	 * 'elevator'
	 */


	public Direction(String description, String type) {
		this.description = description;
		this.type = type;

	}

	public String getDescription() {
		return description;
	}

//    public static String getPath(String type){
//        return path_map.get(type);
//    }

	public String getType() {
		return this.type;
	}
}
