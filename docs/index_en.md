---
title: Validation of metadata fields
identifier: intranda_step_validateMetadataFields
description: Step Plugin for validating metadata fields in METS files
published: true
keywords:
    - Goobi workflow
    - Plugin
    - Step Plugin
    - Validation
    - Metadata
---

## Introduction
This plugin enables automatic validation of metadata fields within METS files in Goobi workflow. It checks configurable rules such as required fields, regular expressions, allowed content, and minimum word counts. If validation errors occur, the workflow step is set to error status and a detailed error message is written to the process log.

## Installation
To be able to use the plugin, the following files must be installed:

```bash
/opt/digiverso/goobi/plugins/step/plugin-step-validate-metadata-fields-base.jar
/opt/digiverso/goobi/config/plugin_intranda_step_validateMetadataFields.xml
```

After installing the plugin, it can be selected within the workflow for the respective steps and will be executed automatically.

To use the plugin, it must be selected in a workflow step:

| Parameter                | Configuration                                   |
|--------------------------|--------------------------------------------------|
| Automatic Task           | Yes                                              |
| Plugin for Workflow Step | intranda_step_validateMetadataFields             |

## Overview and functionality
When executed, the plugin reads the METS file of the process and validates the metadata fields it contains according to the rules defined in the configuration file. The following validations are supported:

- **Required fields**: Checks whether a metadata field is present and contains a non-empty value.
- **Pattern**: Checks whether the value of a field matches a regular expression.
- **Allowed content (list)**: Checks whether the value of a field is contained in an external list of allowed values.
- **Minimum word count**: Checks whether the value of a field contains a minimum number of words.

If validation errors occur, all errors are collected, stored as error messages in the process log, and the workflow step is set to `Error` status. If validation is successful, the workflow step is completed normally.

## Configuration
The plugin is configured in the file `plugin_intranda_step_validateMetadataFields.xml` as shown here:

{{CONFIG_CONTENT}}

{{CONFIG_DESCRIPTION_PROJECT_STEP}}

Each `metadata` element defines the validation rules for a single metadata field. The following attributes are available:

| Parameter                    | Explanation                                                                                                 |
|------------------------------|--------------------------------------------------------------------------------------------------------------|
| `ugh`                        | Name of the metadata field in the ruleset.                                                                  |
| `identifier`                 | Optional identifier for referencing the field (e.g. for `either` or `requiredFields`).                      |
| `required`                   | If set to `true`, the metadata field must be present and contain a non-empty value.                         |
| `requiredErrorMessage`       | Error message displayed when a required field is missing or empty.                                          |
| `pattern`                    | Regular expression that the field value must match.                                                         |
| `patternErrorMessage`        | Error message displayed when the value does not match the pattern.                                          |
| `list`                       | Path to a text file containing allowed values (one value per line).                                         |
| `listErrorMessage`           | Error message displayed when the value is not contained in the list.                                        |
| `wordcount`                  | Minimum number of words the value must contain.                                                             |
| `wordcountErrorMessage`      | Error message displayed when the minimum word count is not met.                                             |
| `either`                     | Identifier of another field where either this or the other must have a value (currently not implemented).   |
| `requiredFields`             | Semicolon-separated list of field identifiers that must be filled for this field to have content (currently not implemented). |
