/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.APIException;
import org.openmrs.test.Verifies;

/**
 * This class tests all methods that are not getter or setters in the Obs java object TODO: finish
 * this test class for Obs
 * 
 * @see Obs
 */
public class ObsTest {
	
	/**
	 * Tests the addToGroup method in ObsGroup
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldAddandRemoveObsToGroup() throws Exception {
		
		Obs obs = new Obs(1);
		
		Obs obsGroup = new Obs(755);
		
		// These methods should not fail even with null attributes on the obs
		assertFalse(obsGroup.isObsGrouping());
		assertFalse(obsGroup.hasGroupMembers(false));
		assertFalse(obsGroup.hasGroupMembers(true)); //Check both flags for false
		
		// adding an obs when the obs group has no other obs
		// should not throw an error
		obsGroup.addGroupMember(obs);
		assertEquals(1, obsGroup.getGroupMembers().size());
		
		// check duplicate add. should only be one
		obsGroup.addGroupMember(obs);
		assertTrue(obsGroup.hasGroupMembers(false));
		assertEquals("Duplicate add should not increase the grouped obs size", 1, obsGroup.getGroupMembers().size());
		
		Obs obs2 = new Obs(2);
		
		obsGroup.removeGroupMember(obs2);
		assertTrue(obsGroup.hasGroupMembers(false));
		assertEquals("Removing a non existent obs should not decrease the number of grouped obs", 1, obsGroup
		        .getGroupMembers().size());
		
		// testing removing an obs from a group that has a null obs list
		new Obs().removeGroupMember(obs2);
		
		obsGroup.removeGroupMember(obs);
		
		assertEquals(0, obsGroup.getGroupMembers().size());
		
		// try to add an obs group to itself
		try {
			obsGroup.addGroupMember(obsGroup);
			fail("An APIException about adding an obsGroup should have been thrown");
		}
		catch (APIException e) {
			// this exception is expected
		}
	}
	
	/**
	 * tests the getRelatedObservations method:
	 */
	@Test
	public void shouldGetRelatedObservations() throws Exception {
		// create a child Obs
		Obs o = new Obs();
		o.setDateCreated(new Date());
		o.setLocation(new Location(1));
		o.setObsDatetime(new Date());
		o.setPerson(new Patient(2));
		o.setValueText("childObs");
		
		//create its sibling
		Obs oSibling = new Obs();
		oSibling.setDateCreated(new Date());
		oSibling.setLocation(new Location(1));
		oSibling.setObsDatetime(new Date());
		oSibling.setValueText("childObs2");
		oSibling.setPerson(new Patient(2));
		
		//create a parent Obs
		Obs oParent = new Obs();
		oParent.setDateCreated(new Date());
		oParent.setLocation(new Location(1));
		oParent.setObsDatetime(new Date());
		oSibling.setValueText("parentObs");
		oParent.setPerson(new Patient(2));
		
		//create a grandparent obs
		Obs oGrandparent = new Obs();
		oGrandparent.setDateCreated(new Date());
		oGrandparent.setLocation(new Location(1));
		oGrandparent.setObsDatetime(new Date());
		oGrandparent.setPerson(new Patient(2));
		oSibling.setValueText("grandParentObs");
		
		oParent.addGroupMember(o);
		oParent.addGroupMember(oSibling);
		oGrandparent.addGroupMember(oParent);
		
		//create a leaf observation at the grandparent level
		Obs o2 = new Obs();
		o2.setDateCreated(new Date());
		o2.setLocation(new Location(1));
		o2.setObsDatetime(new Date());
		o2.setPerson(new Patient(2));
		o2.setValueText("grandparentLeafObs");
		
		oGrandparent.addGroupMember(o2);
		
		/**
		 * test to make sure that if the original child obs calls getRelatedObservations, it returns
		 * itself and its siblings: original obs is one of two groupMembers, so relatedObservations
		 * should return a size of set 2 then, make sure that if oParent calls
		 * getRelatedObservations, it returns its own children as well as the leaf obs attached to
		 * the grandparentObs oParent has two members, and one leaf ancestor -- so a set of size 3
		 * should be returned.
		 */
		assertEquals(o.getRelatedObservations().size(), 2);
		assertEquals(oParent.getRelatedObservations().size(), 3);
		
		// create  a great-grandparent obs
		Obs oGGP = new Obs();
		oGGP.setDateCreated(new Date());
		oGGP.setLocation(new Location(1));
		oGGP.setObsDatetime(new Date());
		oGGP.setPerson(new Patient(2));
		oGGP.setValueText("grandParentObs");
		oGGP.addGroupMember(oGrandparent);
		
		//create a leaf great-grandparent obs
		Obs oGGPleaf = new Obs();
		oGGPleaf.setDateCreated(new Date());
		oGGPleaf.setLocation(new Location(1));
		oGGPleaf.setObsDatetime(new Date());
		oGGPleaf.setPerson(new Patient(2));
		oGGPleaf.setValueText("grandParentObs");
		oGGP.addGroupMember(oGGPleaf);
		
		/**
		 * now run the previous assertions again. this time there are two ancestor leaf obs, so the
		 * first assertion should still return a set of size 2, but the second assertion sould
		 * return a set of size 4.
		 */
		assertEquals(o.getRelatedObservations().size(), 2);
		assertEquals(oParent.getRelatedObservations().size(), 4);
		
		//remove the grandparent leaf observation:
		
		oGrandparent.removeGroupMember(o2);
		
		//now the there is only one ancestor leaf obs:
		assertEquals(o.getRelatedObservations().size(), 2);
		assertEquals(oParent.getRelatedObservations().size(), 3);
		
		/**
		 * finally, test a non-obsGroup and non-member Obs to the function Obs o2 is now not
		 * connected to our heirarchy: an empty set should be returned:
		 */
		
		assertNotNull(o2.getRelatedObservations());
		assertEquals(o2.getRelatedObservations().size(), 0);
		
	}
	
	/**
	 * @see {@link Obs#isComplex()}
	 */
	@Test
	@Verifies(value = "should return true if the concept is complex", method = "isComplex()")
	public void isComplex_shouldReturnTrueIfTheConceptIsComplex() throws Exception {
		ConceptDatatype cd = new ConceptDatatype();
		cd.setName("Complex");
		cd.setHl7Abbreviation("ED");
		
		ConceptComplex complexConcept = new ConceptComplex();
		complexConcept.setDatatype(cd);
		
		Obs obs = new Obs();
		obs.setConcept(complexConcept);
		
		Assert.assertTrue(obs.isComplex());
	}
	
	/**
	 * @see {@link Obs#setValueAsString(String)}
	 */
	@Test(expected = RuntimeException.class)
	@Verifies(value = "should fail if the value of the string is empty", method = "setValueAsString(String)")
	public void setValueAsString_shouldFailIfTheValueOfTheStringIsEmpty() throws Exception {
		Obs obs = new Obs();
		obs.setValueAsString("");
	}
	
	/**
	 * @see {@link Obs#setValueAsString(String)}
	 */
	@Test(expected = RuntimeException.class)
	@Verifies(value = "should fail if the value of the string is null", method = "setValueAsString(String)")
	public void setValueAsString_shouldFailIfTheValueOfTheStringIsNull() throws Exception {
		Obs obs = new Obs();
		obs.setValueAsString(null);
	}
	
	/**
	 * @see {@link Obs#getValueAsBoolean()}
	 */
	@Test
	@Verifies(value = "should return false for value_numeric concepts if value is 0", method = "getValueAsBoolean()")
	public void getValueAsBoolean_shouldReturnFalseForValue_numericConceptsIfValueIs0() throws Exception {
		Obs obs = new Obs();
		obs.setValueNumeric(0.0);
		Assert.assertEquals(false, obs.getValueAsBoolean());
	}
	
	/**
	 * @see {@link Obs#getValueAsBoolean()}
	 */
	@Test
	@Verifies(value = "should return null for value_numeric concepts if value is neither 1 nor 0", method = "getValueAsBoolean()")
	public void getValueAsBoolean_shouldReturnNullForValue_numericConceptsIfValueIsNeither1Nor0() throws Exception {
		Obs obs = new Obs();
		obs.setValueNumeric(24.8);
		Assert.assertNull(obs.getValueAsBoolean());
	}
	
	@Test
	@Verifies(value = "should return non precise values for NumericConcepts", method = "getValueAsString(Locale)")
	public void getValueAsString_shouldReturnNonPreciseValuesForNumericConcepts() throws Exception {
		Obs obs = new Obs();
		obs.setValueNumeric(25.125);
		ConceptNumeric cn = new ConceptNumeric();
		ConceptDatatype cdt = new ConceptDatatype();
		cdt.setHl7Abbreviation("NM");
		cn.setDatatype(cdt);
		cn.setPrecise(false);
		obs.setConcept(cn);
		String str = "25";
		Assert.assertEquals(str, obs.getValueAsString(Locale.US));
	}
	
	@Test
	@Verifies(value = "should return proper DateFormat", method = "getValueAsString()")
	public void getValueAsString_shouldReturnProperDateFormat() throws Exception {
		Obs obs = new Obs();
		obs.setValueDatetime(new Date());
		Concept cn = new Concept();
		ConceptDatatype cdt = new ConceptDatatype();
		cdt.setHl7Abbreviation("DT");
		cn.setDatatype(cdt);
		obs.setConcept(cn);
		
		Date utilDate = new Date();
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
		String dateString = dateFormat.format(utilDate);
		Assert.assertEquals(dateString, obs.getValueAsString(Locale.US));
	}
	
	/**
	 * @see {@link Obs#getValueAsBoolean()}
	 */
	@Test
	@Verifies(value = "should return true for value_numeric concepts if value is 1", method = "getValueAsBoolean()")
	public void getValueAsBoolean_shouldReturnTrueForValue_numericConceptsIfValueIs1() throws Exception {
		Obs obs = new Obs();
		obs.setValueNumeric(1.0);
		Assert.assertEquals(true, obs.getValueAsBoolean());
	}
	
	/**
	 * @see Obs#getGroupMembers(boolean)
	 * @verifies Get all group members if passed true, and non-voided if passed false
	 */
	@Test
	public void getGroupMembers_shouldGetAllGroupMembersIfPassedTrueAndNonvoidedIfPassedFalse() throws Exception {
		Obs parent = new Obs(1);
		Set<Obs> members = new HashSet<Obs>();
		members.add(new Obs(101));
		members.add(new Obs(103));
		Obs voided = new Obs(99);
		voided.setVoided(true);
		members.add(voided);
		parent.setGroupMembers(members);
		members = parent.getGroupMembers(true);
		assertEquals("set of all members should have length of 3", 3, members.size());
		members = parent.getGroupMembers(false);
		assertEquals("set of non-voided should have length of 2", 2, members.size());
		members = parent.getGroupMembers(); //should be same as false
		assertEquals("default should return non-voided with length of 2", 2, members.size());
	}
	
	/**
	 * @see Obs#hasGroupMembers(boolean)
	 * @verifies return true if this obs has group members based on parameter
	 */
	@Test
	public void hasGroupMembers_shouldReturnTrueIfThisObsHasGroupMembersBasedOnParameter() throws Exception {
		Obs parent = new Obs(5);
		Obs child = new Obs(33);
		child.setVoided(true);
		parent.addGroupMember(child); //Only contains 1 voided child
		assertTrue("When checking for all members, should return true", parent.hasGroupMembers(true));
		assertFalse("When checking for non-voided, should return false", parent.hasGroupMembers(false));
		assertFalse("Default should check for non-voided", parent.hasGroupMembers());
	}
	
	/**
	 * @see Obs#isObsGrouping()
	 * @verifies ignore voided Obs
	 */
	@Test
	public void isObsGrouping_shouldIgnoreVoidedObs() throws Exception {
		Obs parent = new Obs(5);
		Obs child = new Obs(33);
		child.setVoided(true);
		parent.addGroupMember(child);
		assertFalse("When checking for Obs grouping, should ignore voided Obs", parent.isObsGrouping());
		child = new Obs(66); //new Child that is non-voided
		parent.addGroupMember(child);
		assertTrue("When there is at least 1 non-voided child, should return True", parent.isObsGrouping());
	}
	
	/**
	 * @see Obs#getComplexValueText()
	 * @verifies that parsed complex value text is valid
	 */
	@Test
	public void getComplexValueText_shouldParseValidComplexValueText() throws Exception {
		String valueComplex = "John Doe|3";
		Obs obs = new Obs(1);
		obs.setValueComplex(valueComplex);
		assertEquals(obs.getComplexValueText(), "John Doe");
		//confirm that valueComplex has not changed
		assertEquals(obs.getValueComplex(), valueComplex);
		
	}
	
	/**
	 * @see Obs#getComplexValueKey()
	 * @verifies that parsed complex value key is valid
	 */
	@Test
	public void getComplexValueKey_shouldParseValidComplexValueKey() throws Exception {
		String valueComplex = "John Doe|3";
		Obs obs = new Obs(1);
		obs.setValueComplex(valueComplex);
		assertEquals(obs.getComplexValueKey(), "3");
		//confirm that valueComplex has not changed
		assertEquals(obs.getValueComplex(), valueComplex);
	}
	
	/**
	 * @see Obs#setComplexValueText()
	 * @verifies that complex value text can be replaced
	 */
	@Test
	public void setComplexValueText_shouldReplaceExistingComplexValueText() throws Exception {
		String valueComplex = "John Doe|3";
		Obs obs = new Obs(1);
		obs.setValueComplex(valueComplex);
		assertEquals(obs.getComplexValueText(), "John Doe");
		
		String newComplexText = "Hubert Hornblower";
		obs.setComplexValueText(newComplexText);
		assertEquals(obs.getComplexValueText(), newComplexText);
		//confirm that valueComplex has been updated
		assertEquals(obs.getValueComplex(), newComplexText + "|" + 3);
	}
	
	/**
	 * @see Obs#setComplexValueKey(String)
	 * @verifies that complex value key can be replaced
	 */
	@Test
	public void setComplexValueKey_shouldReplaceExistingComplexValueKey() throws Exception {
		String valueComplex = "John Doe|3";
		Obs obs = new Obs(1);
		obs.setValueComplex(valueComplex);
		assertEquals(obs.getComplexValueKey(), "3");
		
		String newComplexKey = "5";
		obs.setComplexValueKey(newComplexKey);
		assertEquals(obs.getComplexValueKey(), newComplexKey);
		//confirm that valueComplex has been updated
		assertEquals(obs.getValueComplex(), "John Doe|" + newComplexKey);
	}
	
	/**
	 * @see Obs#setComplexValueText(String)
	 * @verifies that complex value text is not overridden if it is equal to key
	 */
	@Test
	public void setComplexValueText_shouldNotReplaceValueTextIfEqualToKey() throws Exception {
		String valueComplex = "John Doe";
		Obs obs = new Obs(1);
		obs.setValueComplex(valueComplex);
		obs.setComplexValueText(valueComplex);
		assertEquals(obs.getComplexValueText(), valueComplex);
		assertEquals(obs.getComplexValueKey(), valueComplex);
		//confirm that valueComplex doesn't look like "John Doe|John Doe"
		assertEquals(obs.getValueComplex(), "|" + valueComplex);
	}
	
	/**
	 * @see Obs#getComplexValueText()
	 * @verifies that a call to value text will return value key if text is missing
	 */
	@Test
	public void getComplexValueText_shouldReturnKeyIfTextIsMissing() throws Exception {
		String key = "KEY";
		Obs obs = new Obs(1);
		obs.setValueComplex(key);
		assertEquals(obs.getComplexValueText(), key);
		assertEquals(obs.getComplexValueKey(), key);
	}
	
	/**
	 * @see Obs#getComplexValueKey()
	 * @verifies that delimiter is added if missing
	 */
	@Test
	public void setComplexValueKey_shouldAppendDelimiterIfIsMissing() throws Exception {
		String key = "KEY";
		Obs obs = new Obs(1);
		obs.setValueComplex(key);
		obs.setComplexValueText(key);
		//valueComplex should have a delimiter
		assertEquals(obs.getValueComplex(), "|KEY");
	}
	
}
