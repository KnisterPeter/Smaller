// Copyright (c) Microsoft. All rights reserved. Licensed under the Apache License, Version 2.0. 
// See LICENSE.txt in the project root for complete license information.

///<reference path='typescript.ts' />

module TypeScript {
    export class SourceMapping {
        public sourceStartLine: number;
        public sourceStartColumn: number;
        public sourceEndLine: number;
        public sourceEndColumn: number;
        public emittedStartLine: number;
        public emittedStartColumn: number;
        public emittedEndLine: number;
        public emittedEndColumn: number;
        public parent: number;
        public firstChild: number;

        constructor(ast : AST) {
            this.parent = -1;
            this.firstChild = -1;
        }
    }

    export class SourceMapper {
        static MapFileExtension = ".map";
        
        public sourceMappings: SourceMapping[];
        public currentMapping: number;

        public jsFileName: string;
        public tsFileName: string;

        constructor(tsFileName: string, jsFileName: string, public jsFile: ITextWriter, public sourceMapOut: ITextWriter) {
            this.sourceMappings = new SourceMapping[];
            this.currentMapping = -1;

            this.jsFileName = TypeScript.getPrettyName(jsFileName, false, true);
            this.tsFileName = TypeScript.getPrettyName(tsFileName, false, true);
        }
        
        static CanEmitMapping(sourceMappings: SourceMapping[], currentMapping: SourceMapping) {
            if (currentMapping.firstChild !== -1) {
                var childMapping = sourceMappings[currentMapping.firstChild];
                if (childMapping.emittedStartLine === currentMapping.emittedStartLine &&
                    childMapping.emittedStartColumn === currentMapping.emittedStartColumn) {
                    return false;
                }
            }
            return true;
        }

        // Generate source mapping
        static EmitSourceMapping(allSourceMappers: SourceMapper[]) {

            // At this point we know that there is at least one source mapper present.
            // If there are multiple source mappers, all will correspond to same map file but different sources

            // Output map file name into the js file
            var sourceMapper = allSourceMappers[0];
            sourceMapper.jsFile.WriteLine("//@ sourceMappingURL=" + sourceMapper.jsFileName + SourceMapper.MapFileExtension);

            // Now output map file
            var sourceMapOut = sourceMapper.sourceMapOut;
            var mappingsString = "";
            var tsFiles: string[] = [];

            var prevEmittedColumn = 0;
            var prevEmittedLine = 0;
            var prevSourceColumn = 0;
            var prevSourceLine = 0;
            var prevSourceIndex = 0;
            var emitComma = false;
            for (var sourceMapperIndex = 0; sourceMapperIndex < allSourceMappers.length; sourceMapperIndex++) {
                sourceMapper = allSourceMappers[sourceMapperIndex];

                // If there are any mappings generated
                if (sourceMapper.sourceMappings) {
                    var currentSourceIndex = tsFiles.length;
                    tsFiles.push(sourceMapper.tsFileName);
                    
                    var sourceMappings = sourceMapper.sourceMappings;
                    for (var i = 0, len = sourceMappings.length; i < len; i++) {
                        var sourceMapping = sourceMappings[i];
                        if (!SourceMapper.CanEmitMapping(sourceMappings, sourceMapping)) {
                            continue;
                        }

                        if (prevEmittedLine !== sourceMapping.emittedStartLine) {
                            while (prevEmittedLine < sourceMapping.emittedStartLine) {
                                prevEmittedColumn = 0;
                                mappingsString = mappingsString + ";";
                                prevEmittedLine++;
                            }
                            emitComma = false;
                        }
                        else if (emitComma) {
                            mappingsString = mappingsString + ",";
                        }

                        // 1. Relative Column
                        mappingsString = mappingsString + Base64VLQFormat.encode(sourceMapping.emittedStartColumn - prevEmittedColumn);
                        prevEmittedColumn = sourceMapping.emittedStartColumn;

                        // 2. Relative sourceIndex 
                        mappingsString = mappingsString + Base64VLQFormat.encode(currentSourceIndex - prevSourceIndex);
                        prevSourceIndex = currentSourceIndex;

                        // 3. Relative sourceLine 0 based
                        mappingsString = mappingsString + Base64VLQFormat.encode(sourceMapping.sourceStartLine - 1 - prevSourceLine);
                        prevSourceLine = sourceMapping.sourceStartLine - 1;

                        // 4. Relative sourceColumn 0 based 
                        mappingsString = mappingsString + Base64VLQFormat.encode(sourceMapping.sourceStartColumn - prevSourceColumn);
                        prevSourceColumn = sourceMapping.sourceStartColumn;

                        // 5. Since no names , let it go for time being

                        emitComma = true;
                    }
                }
            }

            // Write the actual map file
            if (mappingsString != "") {
                sourceMapOut.Write('{');
                sourceMapOut.Write('"version":3,');
                sourceMapOut.Write('"file":"' + sourceMapper.jsFileName + '",');
                sourceMapOut.Write('"sources":["' + tsFiles.join('","') + '"],');
                sourceMapOut.Write('"names":[],');
                sourceMapOut.Write('"mappings":"' + mappingsString);
                sourceMapOut.Write('"');
                //sourceMapOut.Write('"sourceRoot":""'); // not needed since we arent generating it in the folder
                sourceMapOut.Write('}');
            }

            // Done, close the file
            sourceMapOut.Close();
        }
    }
}
