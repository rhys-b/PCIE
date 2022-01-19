/*
*	Author: Rhys B.
*	Created: 2021-12-29
*	Modified: 2021-12-29
*
*	Contains information on a variable.
*/


public class Variable {
		private String type, name;

		public Variable() {
			this.type = null;
			this.name = null;
		}

		public Variable(String type, String name) {
			this.type = type;
			this.name = name;
		}

		public String getType() {
			return type;
		}

		public String getName() {
			return name;
		}

		public String getSetter() {
			return "\n	public void " + getSetterName() + "(" +
				type + " " + name + ") {\n		this." +
				name + " = " + name + ";\n	}\n";
		}

		public String getGetter() {
			return "\n	public " + type + " " + getGetterName() +
				"() {\n		return " + name + ";\n	}\n";
		}

		public String getSetterName() {
			return "set" + SetterAndGetter.firstUpper(name);
		}

		public String getGetterName() {
			return "get" + SetterAndGetter.firstUpper(name);
		}
	}
