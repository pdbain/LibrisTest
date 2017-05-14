package org.lasalledebain;

import java.util.ArrayList;

import org.lasalledebain.libris.EnumFieldChoices;
import org.lasalledebain.libris.exception.FieldDataException;

public class MockEnumFieldChoices extends EnumFieldChoices {

	private ArrayList<String> choices;

	public MockEnumFieldChoices(String[] choices) {
		setChoices(choices);
	}

	@Override
	public String getChoiceId(int j) throws FieldDataException {
		return choices.get(j);
	}

	@Override
	public String getChoiceValue(int j) throws FieldDataException {
		return choices.get(j);
	}

	@Override
	public int indexFromId(String id) throws FieldDataException {
		int indexOf = choices.indexOf(id);
		if (indexOf < 0) {
			throw new FieldDataException("enum choice "+id+" not found");
		}
		return indexOf;
	}

	public void setChoices(String[] choiceIds) {
		this.choices = new ArrayList<String>();
		for (String s: choiceIds) {
			choices.add(s);
		}
	}
}
