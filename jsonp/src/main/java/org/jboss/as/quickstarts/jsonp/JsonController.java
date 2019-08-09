/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.quickstarts.jsonp;

import java.io.StringReader;
import java.util.stream.IntStream;

import javax.enterprise.inject.Model;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonNumber;
import javax.json.JsonPatch;
import javax.json.JsonPointer;
import javax.json.JsonStructure;
import javax.json.stream.JsonCollectors;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

@Model
public class JsonController {

    @Inject
    private Person person;

    private final String defaultJsonString = "{\"name\":\"Rafael Benevides\",\"age\":35,\"enabled\":true,\"phones\":[\"919-555-1324\",\"919-555-0987\"],\"address\":{\"street\":\"4811 Benevides Street\",\"apt\":456,\"city\":\"Raleigh, NC\",\"zip\":\"27123\"}}";

    private String jsonString = defaultJsonString;

    private String parsedResult;

    private String pointerResult = "empty";

    private String query = "/name";

    private final String mergeJsonString = "{\"name\":\"Rafael\",\"age\":35,\"enabled\":false,\"phones\": null,\"address\":{\"street\":\"4811 Benevides Street\",\"apt\":456,\"city\":\"Raleigh, NC\",\"zip\":\"27123\"}}";

    private final String mergeMinimalJsonString = "{\"name\":\"Rafael\",\"enabled\":false,\"phones\":null}";

    private final String expectedMergeResult = "{\"name\":\"Rafael\",\"age\":35,\"enabled\":false,\"address\":{\"street\":\"4811 Benevides Street\",\"apt\":456,\"city\":\"Raleigh, NC\",\"zip\":\"27123\"}}";

    private final String expectedJsonCollectorResult = "[{\"key1\":\"value1\"},{\"key2\":\"value2\"},{\"key3\":\"value3\"},{\"key4\":\"value4\"},{\"key5\":\"value5\"}]";

    public void generateJson() {
        jsonString = Json.createObjectBuilder()
                .add("name", person.getName())
                .add("age", person.getAge())
                .add("enabled", person.getEnabled())
                .add("phones", Json.createArrayBuilder()
                        .add(person.getPhone1())
                        .add(person.getPhone2())
                )
                .add("address", Json.createObjectBuilder()
                        .add("street", person.getAddressStreet())
                        .add("apt", person.getAddressApt())
                        .add("city", person.getAddressCity())
                        .add("zip", person.getAddressZip())
                )
                .build().toString();
    }


    public String pointerContainsValue() {
        try (StringReader stringReader = new StringReader(jsonString)) {
            final JsonStructure jsonStructure = Json.createReader(stringReader).read();
            JsonPointer pointer = Json.createPointer(query);
            return setAndReturnPointerResult(String.valueOf(pointer.containsValue(jsonStructure)));
        }
    }

    public String pointerGetValue() {
        try (StringReader stringReader = new StringReader(jsonString)) {
            final JsonStructure jsonStructure = Json.createReader(stringReader).read();
            JsonPointer pointer = Json.createPointer(query);
            return setAndReturnPointerResult(String.valueOf(pointer.getValue(jsonStructure)));
        }
    }

    public String pointerAdd() {
        try (StringReader stringReader = new StringReader(jsonString)) {
            JsonStructure jsonStructure = Json.createReader(stringReader).read();
            JsonPointer jsonPointer = Json.createPointer("/children");
            JsonNumber jsonNumber = Json.createValue(3);
            jsonStructure = jsonPointer.add(jsonStructure, jsonNumber);
            return setAndReturnJsonString(String.valueOf(jsonStructure));
        }
    }

    public String pointerReplace() {
        try (StringReader stringReader = new StringReader(jsonString)) {
            JsonStructure jsonStructure = Json.createReader(stringReader).read();
            JsonPointer jsonPointer = Json.createPointer("/age");
            JsonNumber jsonNumberNewValue = Json.createValue(46);
            try {
                jsonStructure = jsonPointer.replace(jsonStructure, jsonNumberNewValue);
                return setAndReturnJsonString(String.valueOf(jsonStructure));
            } catch (Exception e) {
                return setAndReturnJsonString(e.getMessage());
            }
        }
    }

    public String pointerReplaceFail() {
        try (StringReader stringReader = new StringReader(jsonString)) {
            JsonStructure jsonStructure = Json.createReader(stringReader).read();
            JsonPointer jsonPointer = Json.createPointer("/nonsense");
            JsonNumber jsonNumberNewValue = Json.createValue(42);
            try {
                jsonStructure = jsonPointer.replace(jsonStructure, jsonNumberNewValue);
                return setAndReturnJsonString(String.valueOf(jsonStructure));
            } catch (Exception e) {
                return setAndReturnJsonString(e.getMessage());
            }
        }
    }

    public String pointerRemove() {
        JsonPointer jsonPointer = Json.createPointer("/age");
        try (StringReader stringReader = new StringReader(jsonString)) {
            JsonStructure jsonStructure = Json.createReader(stringReader).read();
            try {
                jsonStructure = jsonPointer.remove(jsonStructure);
                return setAndReturnJsonString(String.valueOf(jsonStructure));
            } catch (Exception e) {
                return setAndReturnJsonString(e.getMessage());
            }
        }
    }

    public String patchAdd() {
        JsonPatch jsonPatch = Json.createPatchBuilder().add("/friends", 12).build();
        try (StringReader stringReader = new StringReader(jsonString)) {
            JsonStructure jsonStructure = Json.createReader(stringReader).read();
            try {
                return setAndReturnJsonString(String.valueOf(jsonPatch.apply(jsonStructure)));
            } catch (Exception e) {
                return setAndReturnJsonString(e.getMessage());
            }
        }
    }

    public String patchRemove() {
        JsonPatch jsonPatch = Json.createPatchBuilder().remove("/age").build();
        try (StringReader stringReader = new StringReader(jsonString)) {
            JsonStructure jsonStructure = Json.createReader(stringReader).read();
            try {
                return setAndReturnJsonString(String.valueOf(jsonPatch.apply(jsonStructure)));
            } catch (Exception e) {
                return setAndReturnJsonString(e.getMessage());
            }
        }
    }

    public String patchMove() {
        JsonPatch jsonPatch = Json.createPatchBuilder().move("/name", "/age").build();
        try (StringReader stringReader = new StringReader(jsonString)) {
            JsonStructure jsonStructure = Json.createReader(stringReader).read();
            try {
                return setAndReturnJsonString(String.valueOf(jsonPatch.apply(jsonStructure)));
            } catch (Exception e) {
                return setAndReturnJsonString(e.getMessage());
            }
        }
    }

    public String patchCopy() {
        JsonPatch jsonPatch = Json.createPatchBuilder().copy("/name", "/age").build();
        try (StringReader stringReader = new StringReader(jsonString)) {
            JsonStructure jsonStructure = Json.createReader(stringReader).read();
            try {
                return setAndReturnJsonString(String.valueOf(jsonPatch.apply(jsonStructure)));
            } catch (Exception e) {
                return setAndReturnJsonString(e.getMessage());
            }
        }
    }

    public String patchCopyFail() {
        JsonPatch jsonPatch = Json.createPatchBuilder().copy("/name", "/nonsense").build();
        try (StringReader stringReader = new StringReader(jsonString)) {
            JsonStructure jsonStructure = Json.createReader(stringReader).read();
            try {
                return setAndReturnJsonString(String.valueOf(jsonPatch.apply(jsonStructure)));
            } catch (Exception e) {
                return setAndReturnJsonString(e.getMessage());
            }
        }
    }

    public String patchReplace() {
        JsonPatch jsonPatch = Json.createPatchBuilder().replace("/age", 56).build();
        try (StringReader stringReader = new StringReader(jsonString)) {
            JsonStructure jsonStructure = Json.createReader(stringReader).read();
            try {
                return setAndReturnJsonString(String.valueOf(jsonPatch.apply(jsonStructure)));
            } catch (Exception e) {
                return setAndReturnJsonString(e.getMessage());
            }
        }
    }

    public String mergePatch(boolean minimal) {
        String mergeString = minimal ? this.mergeMinimalJsonString : this.mergeJsonString;
        try (StringReader readerDefault = new StringReader(defaultJsonString);
             StringReader readerMerge = new StringReader(mergeString);
        ) {
            JsonStructure structureDefault = Json.createReader(readerDefault).read();
            JsonStructure structureMerge = Json.createReader(readerMerge).read();
            try {
                return setAndReturnJsonString(String.valueOf(Json.createMergePatch(structureMerge).apply(structureDefault)));
            } catch (Exception e) {
                return setAndReturnJsonString(e.getMessage());
            }
        }
    }

    public String mergeDiff() {
        try (StringReader readerDefault = new StringReader(defaultJsonString);
             StringReader readerResult = new StringReader(expectedMergeResult);
        ) {
            JsonStructure structureDefault = Json.createReader(readerDefault).read();
            JsonStructure structureResult = Json.createReader(readerResult).read();
            try {
                return setAndReturnJsonString(String.valueOf(Json.createMergeDiff(structureDefault, structureResult).toJsonValue()));
            } catch (Exception e) {
                return setAndReturnJsonString(e.getMessage());
            }
        }
    }

    public String jsonCollector() {
        JsonArray array = IntStream.rangeClosed(1, 5)
                .mapToObj(i -> Json.createObjectBuilder()
                        .add("key" + i, "value" + i)
                        .build())
                .collect(JsonCollectors.toJsonArray());
        return setAndReturnJsonString(String.valueOf(array));
    }

    public void parseJsonStream() {
        StringBuilder sb = new StringBuilder();
        String json = getJsonString();
        try {
            JsonParser parser = Json.createParser(new StringReader(json));
            while (parser.hasNext()) {
                Event event = parser.next();
                if (event.equals(Event.KEY_NAME)) {
                    sb.append(" - - - -  >  Key: " + parser.getString() + "  < - - - - - \n");
                }
                if (event.equals(Event.VALUE_STRING)) {
                    sb.append("Value as String: " + parser.getString() + "\n");
                }
                if (event.equals(Event.VALUE_NUMBER)) {
                    sb.append("Value as Number: " + parser.getInt() + "\n");
                }
                if (event.equals(Event.VALUE_TRUE)) {
                    sb.append("Value as Boolean: true\n");
                }
                if (event.equals(Event.VALUE_FALSE)) {
                    sb.append("Value as Boolean: false \n");
                }
            }
        } catch (JsonException e) {
            FacesContext.getCurrentInstance().addMessage("form:parsed", new FacesMessage(e.getMessage()));
        }
        parsedResult = sb.toString();
    }

    public void setJsonString(String jsonString) {
        this.jsonString = jsonString;
    }

    public String setAndReturnJsonString(String jsonString) {
        setJsonString(jsonString);
        return this.jsonString;
    }

    public String getJsonString() {
        return jsonString;
    }

    public String getParsedResult() {
        return parsedResult;
    }

    public String getPointerResult() {
        return pointerResult;
    }

    public void setPointerResult(String pointerResult) {
        this.pointerResult = pointerResult;
    }

    public String setAndReturnPointerResult(String pointerResult) {
        setPointerResult(pointerResult);
        return this.pointerResult;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
