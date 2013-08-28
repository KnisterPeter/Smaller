// Copyright (c) Microsoft. All rights reserved. Licensed under the Apache License, Version 2.0. 
// See LICENSE.txt in the project root for complete license information.

///<reference path='typescript.ts' />

module TypeScript {
    export class AST {
        public type: Type = null;
        public flags = ASTFlags.Writeable;

        public minChar: number = -1;  // -1 = "undefined" or "compiler generated"
        public limChar: number = -1;  // -1 = "undefined" or "compiler generated"

        // REVIEW: for diagnostic purposes
        public passCreated: number = CompilerDiagnostics.analysisPass;

        public preComments: Comment[] = null;
        public postComments: Comment[] = null;

        public isParenthesized = false;

        constructor (public nodeType: NodeType) { }

        public isStatementOrExpression() { return false; }
        public isCompoundStatement() { return false; }
        public isLeaf() { return this.isStatementOrExpression() && (!this.isCompoundStatement()); }

        public typeCheck(typeFlow: TypeFlow) {
            switch (this.nodeType) {
                case NodeType.Error:
                case NodeType.EmptyExpr:
                    this.type = typeFlow.anyType;
                    break;
                case NodeType.This:
                    return typeFlow.typeCheckThis(this);
                case NodeType.Null:
                    this.type = typeFlow.nullType;
                    break;
                case NodeType.False:
                case NodeType.True:
                    this.type = typeFlow.booleanType;
                    break;
                case NodeType.Super:
                    return typeFlow.typeCheckSuper(this);
                case NodeType.EndCode:
                case NodeType.Empty:
                case NodeType.Void:
                    this.type = typeFlow.voidType;
                    break;
                default:
                    throw new Error("please implement in derived class");
            }
            return this;
        }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            emitter.emitParensAndCommentsInPlace(this, true);
            emitter.recordSourceMappingStart(this);
            switch (this.nodeType) {
                case NodeType.This:
                    if (emitter.thisFnc && (hasFlag(emitter.thisFnc.fncFlags, FncFlags.IsFatArrowFunction))) {
                        emitter.writeToOutput("_this");
                    }
                    else {
                        emitter.writeToOutput("this");
                    }
                    break;
                case NodeType.Null:
                    emitter.writeToOutput("null");
                    break;
                case NodeType.False:
                    emitter.writeToOutput("false");
                    break;
                case NodeType.True:
                    emitter.writeToOutput("true");
                    break;
                case NodeType.Super:
                    emitter.emitSuperReference();
                case NodeType.EndCode:
                    break;
                case NodeType.Error:
                case NodeType.EmptyExpr:
                    break;

                case NodeType.Empty:
                    emitter.writeToOutput("; ");
                    break;
                case NodeType.Void:
                    emitter.writeToOutput("void ");
                    break;
                default:
                    throw new Error("please implement in derived class");
            }
            emitter.recordSourceMappingEnd(this);
            emitter.emitParensAndCommentsInPlace(this, false);
        }

        public print(context: PrintContext) {
            context.startLine();
            var lineCol = { line: -1, col: -1 };
            var limLineCol = { line: -1, col: -1 };
            if (context.parser !== null) {
                context.parser.getSourceLineCol(lineCol, this.minChar);
                context.parser.getSourceLineCol(limLineCol, this.limChar);
                context.write("(" + lineCol.line + "," + lineCol.col + ")--" +
                              "(" + limLineCol.line + "," + limLineCol.col + "): ");
            }
            var lab = this.printLabel();
            if (hasFlag(this.flags, ASTFlags.Error)) {
                lab += " (Error)";
            }
            context.writeLine(lab);
        }

        public printLabel() {
            if (nodeTypeTable[this.nodeType] !== undefined) {
                return nodeTypeTable[this.nodeType];
            }
            else {
                return (<any>NodeType)._map[this.nodeType];
            }
        }

        public addToControlFlow(context: ControlFlowContext): void {
            // by default, AST adds itself to current basic block and does not check its children
            context.walker.options.goChildren = false;
            context.addContent(this);
        }

        public netFreeUses(container: Symbol, freeUses: StringHashTable) {
        }

        public treeViewLabel() {
            return (<any>NodeType)._map[this.nodeType];
        }
    }

    export class IncompleteAST extends AST {
        constructor (min: number, lim: number) {
            super(NodeType.Error);

            this.minChar = min;
            this.limChar = lim;
        }
    }

    export class ASTList extends AST {
        public enclosingScope: SymbolScope = null;
        public members: AST[] = new AST[];

        constructor () {
            super(NodeType.List);
        }

        public addToControlFlow(context: ControlFlowContext) {
            var len = this.members.length;
            for (var i = 0; i < len; i++) {
                if (context.noContinuation) {
                    context.addUnreachable(this.members[i]);
                    break;
                }
                else {
                    this.members[i] = context.walk(this.members[i], this);
                }
            }
            context.walker.options.goChildren = false;
        }

        public append(ast: AST) {
            this.members[this.members.length] = ast;
            return this;
        }

        public appendAll(ast: AST) {
            if (ast.nodeType == NodeType.List) {
                var list = <ASTList>ast;
                for (var i = 0, len = list.members.length; i < len; i++) {
                    this.append(list.members[i]);
                }
            }
            else {
                this.append(ast);
            }
            return this;
        }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            emitter.emitJavascriptList(this, null, TokenID.SColon, startLine, false, false, writeDeclFile);
        }

        public typeCheck(typeFlow: TypeFlow) {
            var len = this.members.length;
            typeFlow.nestingLevel++;
            for (var i = 0; i < len; i++) {
                if (this.members[i]) {
                    this.members[i] = this.members[i].typeCheck(typeFlow);
                }
            }
            typeFlow.nestingLevel--;
            return this;
        }
    }

    export class Identifier extends AST {
        public sym: Symbol = null;
        public cloId = -1;

        constructor (public text: string) {
            super(NodeType.Name);
        }

        public isMissing() { return false; }
        public isLeaf() { return true; }

        public treeViewLabel() {
            return "id: " + this.text;
        }

        public printLabel() {
            if (this.text) {
                return "id: " + this.text;
            }
            else {
                return "name node";
            }
        }

        public typeCheck(typeFlow: TypeFlow) {
            return typeFlow.typeCheckName(this);
        }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            emitter.emitJavascriptName(this, true);
        }

    }

    export class MissingIdentifier extends Identifier {
        constructor () {
            super("__missing");
        }
        public isMissing() { return true; }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            // Emit nothing for a missing ID
        }
    }

    export class Label extends AST {
        constructor (public id: Identifier) {
            super(NodeType.Label);
        }

        public printLabel() { return this.id.text + ":"; }

        public typeCheck(typeFlow: TypeFlow) {
            this.type = typeFlow.voidType;
            return this;
        }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            emitter.emitParensAndCommentsInPlace(this, true);
            emitter.recordSourceMappingStart(this);
            emitter.writeLineToOutput(this.id.text + ":");
            emitter.recordSourceMappingEnd(this);
            emitter.emitParensAndCommentsInPlace(this, false);
        }

    }

    export class UnaryExpression extends AST {

        public targetType: Type = null; // Target type for an object literal (null if no target type)
        public castTerm: AST = null;
        public nty: NodeType;

        constructor (nty: number, public operand: AST) {
            super(nty);
            this.nty = nty;
        }

        public isStatementOrExpression() { return true; }

        public addToControlFlow(context: ControlFlowContext): void {
            super.addToControlFlow(context);
            // TODO: add successor as catch block/finally block if present
            if (this.nodeType == NodeType.Throw) {
                context.returnStmt();
            }
        }

        public typeCheck(typeFlow: TypeFlow) {
            switch (this.nty) {
                case NodeType.Not:
                    return typeFlow.typeCheckBitNot(this);

                case NodeType.LogNot:
                    return typeFlow.typeCheckLogNot(this);

                case NodeType.Pos:
                case NodeType.Neg:
                    return typeFlow.typeCheckUnaryNumberOperator(this);

                case NodeType.IncPost:
                case NodeType.IncPre:
                case NodeType.DecPost:
                case NodeType.DecPre:
                    return typeFlow.typeCheckIncOrDec(this);

                case NodeType.ArrayLit:
                    typeFlow.typeCheckArrayLit(this);
                    return this;

                case NodeType.ObjectLit:
                    typeFlow.typeCheckObjectLit(this);
                    return this;

                case NodeType.Throw:
                    this.operand = typeFlow.typeCheck(this.operand);
                    this.type = typeFlow.voidType;
                    return this;

                case NodeType.Typeof:
                    this.operand = typeFlow.typeCheck(this.operand);
                    this.type = typeFlow.stringType;
                    return this;

                case NodeType.Delete:
                    this.operand = typeFlow.typeCheck(this.operand);
                    this.type = typeFlow.booleanType;
                    break;

                case NodeType.TypeAssertion:
                    this.castTerm = typeFlow.typeCheck(this.castTerm);
                    var applyTargetType = !this.operand.isParenthesized;

                    var targetType = applyTargetType ? this.castTerm.type : null;

                    typeFlow.checker.typeCheckWithContextualType(targetType, typeFlow.checker.inProvisionalTypecheckMode(), true, this.operand);
                    typeFlow.castWithCoercion(this.operand, this.castTerm.type, false, true);
                    this.type = this.castTerm.type;
                    return this;

                case NodeType.Void:
                    // REVIEW - Although this is good to do for completeness's sake,
                    // this shouldn't be strictly necessary from the void operator's
                    // point of view
                    this.operand = typeFlow.typeCheck(this.operand);
                    this.type = typeFlow.checker.undefinedType;
                    break;

                default:
                    throw new Error("please implement in derived class");
            }
            return this;
        }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            emitter.emitParensAndCommentsInPlace(this, true);
            emitter.recordSourceMappingStart(this);
            switch (this.nty) {
                case NodeType.IncPost:
                    emitter.emitJavascript(this.operand, TokenID.Inc, false);
                    emitter.writeToOutput("++");
                    break;
                case NodeType.LogNot:
                    emitter.writeToOutput("!");
                    emitter.emitJavascript(this.operand, TokenID.Bang, false);
                    break;
                case NodeType.DecPost:
                    emitter.emitJavascript(this.operand, TokenID.Dec, false);
                    emitter.writeToOutput("--");
                    break;
                case NodeType.ObjectLit:
                    emitter.emitObjectLiteral(<ASTList>this.operand);
                    break;
                case NodeType.ArrayLit:
                    emitter.emitArrayLiteral(<ASTList>this.operand);
                    break;
                case NodeType.Not:
                    emitter.writeToOutput("~");
                    emitter.emitJavascript(this.operand, TokenID.Tilde, false);
                    break;
                case NodeType.Neg:
                    emitter.writeToOutput("-");
                    if (this.operand.nodeType == NodeType.Neg) {
                        this.operand.isParenthesized = true;
                    }
                    emitter.emitJavascript(this.operand, TokenID.Sub, false);
                    break;
                case NodeType.Pos:
                    emitter.writeToOutput("+");
                    if (this.operand.nodeType == NodeType.Pos) {
                        this.operand.isParenthesized = true;
                    }
                    emitter.emitJavascript(this.operand, TokenID.Add, false);
                    break;
                case NodeType.IncPre:
                    emitter.writeToOutput("++");
                    emitter.emitJavascript(this.operand, TokenID.Inc, false);
                    break;
                case NodeType.DecPre:
                    emitter.writeToOutput("--");
                    emitter.emitJavascript(this.operand, TokenID.Dec, false);
                    break;
                case NodeType.Throw:
                    emitter.writeToOutput("throw ");
                    emitter.emitJavascript(this.operand, TokenID.Tilde, false);
                    emitter.writeToOutput(";");
                    break;
                case NodeType.Typeof:
                    emitter.writeToOutput("typeof ");
                    emitter.emitJavascript(this.operand, TokenID.Tilde, false);
                    break;
                case NodeType.Delete:
                    emitter.writeToOutput("delete ");
                    emitter.emitJavascript(this.operand, TokenID.Tilde, false);
                    break;
                case NodeType.Void:
                    emitter.writeToOutput("void ");
                    emitter.emitJavascript(this.operand, TokenID.Tilde, false);
                    break;
                case NodeType.TypeAssertion:
                    emitter.emitJavascript(this.operand, TokenID.Tilde, false);
                    break;
                default:
                    throw new Error("please implement in derived class");
            }
            emitter.recordSourceMappingEnd(this);
            emitter.emitParensAndCommentsInPlace(this, false);
        }

    }

    export class CallExpression extends AST {

        public nty: NodeType;

        constructor (nty: number, public target: AST, public args: ASTList) {
            super(nty);
            this.nty = nty;
            this.minChar = this.target.minChar;
        }

        public signature: Signature = null;
        public isStatementOrExpression() { return true; }
        public typeCheck(typeFlow: TypeFlow) {
            if (this.nty == NodeType.New) {
                return typeFlow.typeCheckNew(this);
            }
            else {
                return typeFlow.typeCheckCall(this);
            }
        }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            emitter.emitParensAndCommentsInPlace(this, true);
            emitter.recordSourceMappingStart(this);

            if (this.nty == NodeType.New) {
                emitter.emitNew(this.target, this.args);
            }
            else {
                emitter.emitCall(this, this.target, this.args);
            }

            emitter.recordSourceMappingEnd(this);
            emitter.emitParensAndCommentsInPlace(this, false);
        }
    }

    export class BinaryExpression extends AST {
        public nty: NodeType;

        constructor (nty: number, public operand1: AST, public operand2: AST) {
            super(nty);
            this.nty = nty;
        }

        public isStatementOrExpression() { return true; }

        public typeCheck(typeFlow: TypeFlow) {
            switch (this.nty) {
                case NodeType.Dot:
                    return typeFlow.typeCheckDotOperator(this);
                case NodeType.Asg:
                    return typeFlow.typeCheckAsgOperator(this);
                case NodeType.Add:
                case NodeType.Sub:
                case NodeType.Mul:
                case NodeType.Div:
                case NodeType.Mod:
                case NodeType.Or:
                case NodeType.And:
                    return typeFlow.typeCheckArithmeticOperator(this, false);
                case NodeType.Xor:
                    return typeFlow.typeCheckBitwiseOperator(this, false);
                case NodeType.Ne:
                case NodeType.Eq:
                    var text: string;
                    if (typeFlow.checker.styleSettings.eqeqeq) {
                        text = nodeTypeTable[this.nty];
                        typeFlow.checker.errorReporter.styleError(this, "use of " + text);
                    }
                    else if (typeFlow.checker.styleSettings.eqnull) {
                        text = nodeTypeTable[this.nty];
                        if ((this.operand2 !== null) && (this.operand2.nodeType == NodeType.Null)) {
                            typeFlow.checker.errorReporter.styleError(this, "use of " + text + " to compare with null");
                        }
                    }
                case NodeType.Eqv:
                case NodeType.NEqv:
                case NodeType.Lt:
                case NodeType.Le:
                case NodeType.Ge:
                case NodeType.Gt:
                    return typeFlow.typeCheckBooleanOperator(this);
                case NodeType.Index:
                    return typeFlow.typeCheckIndex(this);
                case NodeType.Member:
                    this.type = typeFlow.voidType;
                    return this;
                case NodeType.LogOr:
                    return typeFlow.typeCheckLogOr(this);
                case NodeType.LogAnd:
                    return typeFlow.typeCheckLogAnd(this);
                case NodeType.AsgAdd:
                case NodeType.AsgSub:
                case NodeType.AsgMul:
                case NodeType.AsgDiv:
                case NodeType.AsgMod:
                case NodeType.AsgOr:
                case NodeType.AsgAnd:
                    return typeFlow.typeCheckArithmeticOperator(this, true);
                case NodeType.AsgXor:
                    return typeFlow.typeCheckBitwiseOperator(this, true);
                case NodeType.Lsh:
                case NodeType.Rsh:
                case NodeType.Rs2:
                    return typeFlow.typeCheckShift(this, false);
                case NodeType.AsgLsh:
                case NodeType.AsgRsh:
                case NodeType.AsgRs2:
                    return typeFlow.typeCheckShift(this, true);
                case NodeType.Comma:
                    return typeFlow.typeCheckCommaOperator(this);
                case NodeType.InstOf:
                    return typeFlow.typeCheckInstOf(this);
                case NodeType.In:
                    return typeFlow.typeCheckInOperator(this);
                case NodeType.From:
                    typeFlow.checker.errorReporter.simpleError(this, "Illegal use of 'from' keyword in binary expression");
                    break;
                default:
                    throw new Error("please implement in derived class");
            }
            return this;
        }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            var binTokenId = nodeTypeToTokTable[this.nodeType];

            emitter.emitParensAndCommentsInPlace(this, true);
            emitter.recordSourceMappingStart(this);
            if (binTokenId != undefined) {

                emitter.emitJavascript(this.operand1, binTokenId, false);

                if (tokenTable[binTokenId].text == "instanceof") {
                    emitter.writeToOutput(" instanceof ");
                }
                else if (tokenTable[binTokenId].text == "in") {
                    emitter.writeToOutput(" in ");
                }
                else {
                    emitter.writeToOutputTrimmable(" " + tokenTable[binTokenId].text + " ");
                }

                emitter.emitJavascript(this.operand2, binTokenId, false);
            }
            else {
                switch (this.nty) {
                    case NodeType.Dot:
                        if (!emitter.tryEmitConstant(this)) {
                            emitter.emitJavascript(this.operand1, TokenID.Dot, false);
                            emitter.writeToOutput(".");
                            emitter.emitJavascriptName(<Identifier>this.operand2, false);
                        }
                        break;
                    case NodeType.Index:
                        emitter.emitIndex(this.operand1, this.operand2);
                        break;

                    case NodeType.Member:
                        if (this.operand2.nodeType == NodeType.FuncDecl && (<FuncDecl>this.operand2).isAccessor()) {
                            var funcDecl = <FuncDecl>this.operand2;
                            if (hasFlag(funcDecl.fncFlags, FncFlags.GetAccessor)) {
                                emitter.writeToOutput("get ");
                            }
                            else {
                                emitter.writeToOutput("set ");
                            }
                            emitter.emitJavascript(this.operand1, TokenID.Colon, false);
                        }
                        else {
                            emitter.emitJavascript(this.operand1, TokenID.Colon, false);
                            emitter.writeToOutputTrimmable(": ");
                        }
                        emitter.emitJavascript(this.operand2, TokenID.Comma, false);
                        break;
                    case NodeType.Comma:
                        emitter.emitJavascript(this.operand1, TokenID.Comma, false);
                        if (emitter.emitState.inObjectLiteral) {
                            emitter.writeLineToOutput(", ");
                        }
                        else {
                            emitter.writeToOutput(",");
                        }
                        emitter.emitJavascript(this.operand2, TokenID.Comma, false);
                        break;
                    case NodeType.Is:
                        throw new Error("should be de-sugared during type check");
                    default:
                        throw new Error("please implement in derived class");
                }
            }
            emitter.recordSourceMappingEnd(this);
            emitter.emitParensAndCommentsInPlace(this, false);
        }

    }

    export class TrinaryExpression extends AST {
        public nty: NodeType;

        constructor (nty: number, public operand1: AST, public operand2: AST,
                            public operand3: AST) {
            super(nty);
            this.nty = nty;
        }

        public isStatementOrExpression() { return true; }
        public typeCheck(typeFlow: TypeFlow) {
            return typeFlow.typeCheckQMark(this);
        }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            emitter.emitParensAndCommentsInPlace(this, true);
            emitter.recordSourceMappingStart(this);
            emitter.emitJavascript(this.operand1, TokenID.QMark, false);
            emitter.writeToOutput(" ? ");
            emitter.emitJavascript(this.operand2, TokenID.QMark, false);
            emitter.writeToOutput(" : ");
            emitter.emitJavascript(this.operand3, TokenID.QMark, false);
            emitter.recordSourceMappingEnd(this);
            emitter.emitParensAndCommentsInPlace(this, false);
        }
    }

    export class NumberLiteral extends AST {

        constructor (public value: number) {
            super(NodeType.NumberLit);
        }

        public isStatementOrExpression() { return true; }
        public isNegativeZero = false;
        public typeCheck(typeFlow: TypeFlow) {
            this.type = typeFlow.doubleType;
            return this;
        }
        public treeViewLabel() {
            return "num: " + this.value;
        }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            emitter.emitParensAndCommentsInPlace(this, true);
            emitter.recordSourceMappingStart(this);
            if (this.isNegativeZero) {
                emitter.writeToOutput("-");
            }

            emitter.writeToOutput(this.value.toString());
            emitter.recordSourceMappingEnd(this);
            emitter.emitParensAndCommentsInPlace(this, false);
        }

        public printLabel() {
            if (Math.floor(this.value) != this.value) {
                return this.value.toFixed(2).toString();
            }
            else {
                return this.value.toString();
            }
        }
    }

    export class RegexLiteral extends AST {

        constructor (public regex) {
            super(NodeType.Regex);
        }

        public isStatementOrExpression() { return true; }
        public typeCheck(typeFlow: TypeFlow) {
            this.type = typeFlow.regexType;
            return this;
        }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            emitter.emitParensAndCommentsInPlace(this, true);
            emitter.recordSourceMappingStart(this);
            emitter.writeToOutput(this.regex.toString());
            emitter.recordSourceMappingEnd(this);
            emitter.emitParensAndCommentsInPlace(this, false);
        }
    }

    export class StringLiteral extends AST {

        constructor (public text: string) {
            super(NodeType.QString);
        }

        public isStatementOrExpression() { return true; }
        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            emitter.emitParensAndCommentsInPlace(this, true);
            emitter.recordSourceMappingStart(this);
            emitter.emitStringLiteral(this.text);
            emitter.recordSourceMappingEnd(this);
            emitter.emitParensAndCommentsInPlace(this, false);
        }

        public typeCheck(typeFlow: TypeFlow) {
            this.type = typeFlow.stringType;
            return this;
        }

        public treeViewLabel() {
            return "st: " + this.text;
        }

        public printLabel() {
            return this.text;
        }
    }

    export class ImportDecl extends AST {
        public isStatementOrExpression() { return true; }
        public varFlags = VarFlags.None;
        public isDynamicImport = false;

        constructor (public id: Identifier, public alias: AST) {
            super(NodeType.Import);
        }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            var mod = <ModuleType>this.alias.type;
            if (writeDeclFile) {
                emitter.emitImportDecl(this);
            }

            // REVIEW: Only modules may be aliased for now, though there's no real
            // restriction on what the type symbol may be
            if (!this.isDynamicImport || (this.id.sym && !(<TypeSymbol>this.id.sym).onlyReferencedAsTypeRef)) {
                var prevModAliasId = emitter.modAliasId;
                var prevFirstModAlias = emitter.firstModAlias;

                emitter.recordSourceMappingStart(this);
                emitter.emitParensAndCommentsInPlace(this, true);
                emitter.writeToOutput("var " + this.id.text + " = ");
                emitter.modAliasId = this.id.text;
                emitter.firstModAlias = this.firstAliasedModToString();
                emitter.emitJavascript(this.alias, TokenID.Tilde, false, writeDeclFile);
                // the dynamic import case will insert the semi-colon automatically
                if (!this.isDynamicImport) {
                    emitter.writeToOutput(";");
                }
                emitter.emitParensAndCommentsInPlace(this, false);
                emitter.recordSourceMappingEnd(this);

                emitter.modAliasId = prevModAliasId;
                emitter.firstModAlias = prevFirstModAlias;
            }
        }

        public typeCheck(typeFlow: TypeFlow) {
            return typeFlow.typeCheckImportDecl(this);
        }

        public getAliasName(aliasAST?: AST = this.alias) {
            if (aliasAST.nodeType == NodeType.Name) {
                return (<Identifier>aliasAST).text;
            } else {
                var dotExpr = <BinaryExpression>aliasAST;
                return this.getAliasName(dotExpr.operand1) + "." + this.getAliasName(dotExpr.operand2);
            }
        }

        public firstAliasedModToString() {
            if (this.alias.nodeType == NodeType.Name) {
                return (<Identifier>this.alias).text;
            }
            else {
                var dotExpr = <BinaryExpression>this.alias;
                var firstMod = <Identifier>dotExpr.operand1;
                return firstMod.text;
            }
        }
    }

    export class BoundDecl extends AST {
        public init: AST = null;
        public typeExpr: AST = null;
        public varFlags = VarFlags.None;
        public sym: Symbol = null;

        constructor (public id: Identifier, nodeType: NodeType, public nestingLevel: number) {
            super(nodeType);
        }

        public isStatementOrExpression() { return true; }

        public isPrivate() { return hasFlag(this.varFlags, VarFlags.Private); }
        public isPublic() { return hasFlag(this.varFlags, VarFlags.Public); }
        public isProperty() { return hasFlag(this.varFlags, VarFlags.Property); }

        public typeCheck(typeFlow: TypeFlow) {
            return typeFlow.typeCheckBoundDecl(this);
        }

        public printLabel() {
            return this.treeViewLabel();
        }
    }

    export class VarDecl extends BoundDecl {

        constructor (id: Identifier, nest: number) {
            super(id, NodeType.VarDecl, nest);
        }

        public isAmbient() { return hasFlag(this.varFlags, VarFlags.Ambient); }
        public isExported() { return hasFlag(this.varFlags, VarFlags.Exported); }
        public isStatic() { return hasFlag(this.varFlags, VarFlags.Static); }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            emitter.emitJavascriptVarDecl(this, tokenId, writeDeclFile);
        }

        public treeViewLabel() {
            return "var " + this.id.text;
        }
    }

    export class ArgDecl extends BoundDecl {
        constructor (id: Identifier) {
            super(id, NodeType.ArgDecl, 0);
        }

        public isOptional = false;

        public isOptionalArg() { return this.isOptional || this.init; }

        public treeViewLabel() {
            return "arg: " + this.id.text;
        }

        public parameterPropertySym: FieldSymbol = null;

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            emitter.emitParensAndCommentsInPlace(this, true);
            emitter.recordSourceMappingStart(this);
            emitter.writeToOutput(this.id.text);
            if (writeDeclFile) {
                emitter.emitArgDecl(this);
            }
            emitter.recordSourceMappingEnd(this);
            emitter.emitParensAndCommentsInPlace(this, false);
        }
    }

    var internalId = 0;

    export class FuncDecl extends AST {
        public hint: string = null;
        public fncFlags = FncFlags.None;
        public returnTypeAnnotation: AST = null;
        public symbols: IHashTable;
        public variableArgList = false;
        public signature: Signature;
        public envids: Identifier[];
        public jumpRefs: Identifier[] = null;
        public internalNameCache: string = null;
        public tmp1Declared = false;
        public enclosingFnc: FuncDecl = null;
        public freeVariables: Symbol[] = [];
        public unitIndex = -1;
        public classDecl: Record = null;
        public boundToProperty: VarDecl = null;
        public isOverload = false;
        public innerStaticFuncs: FuncDecl[] = [];
        public isTargetTypedAsMethod = false;
        public isInlineCallLiteral = false;
        public accessorSymbol: Symbol = null;
        public leftCurlyCount = 0;
        public rightCurlyCount = 0;
        public returnStatementsWithExpressions: ReturnStatement[] = [];
        public scopeType: Type = null; // Type of the FuncDecl, before target typing

        constructor (public name: Identifier, public bod: ASTList, public isConstructor: bool,
                    public args: ASTList, public vars: ASTList, public scopes: ASTList, public statics: ASTList,
            nodeType: number) {

            super(nodeType);
        }

        public internalName(): string {
            if (this.internalNameCache == null) {
                var extName = this.getNameText();
                if (extName) {
                    this.internalNameCache = "_internal_" + extName;
                }
                else {
                    this.internalNameCache = "_internal_" + internalId++;
                }
            }
            return this.internalNameCache;
        }

        public hasSelfReference() { return hasFlag(this.fncFlags, FncFlags.HasSelfReference); }
        public setHasSelfReference() { this.fncFlags |= FncFlags.HasSelfReference; }

        public addCloRef(id: Identifier, sym: Symbol): number {
            if (this.envids == null) {
                this.envids = new Identifier[];
            }
            this.envids[this.envids.length] = id;
            var outerFnc = this.enclosingFnc;
            if (sym) {
                while (outerFnc && (outerFnc.type.symbol != sym.container)) {
                    outerFnc.addJumpRef(sym);
                    outerFnc = outerFnc.enclosingFnc;
                }
            }
            return this.envids.length - 1;
        }

        public addJumpRef(sym: Symbol): void {
            if (this.jumpRefs == null) {
                this.jumpRefs = new Identifier[];
            }
            var id = new Identifier(sym.name);
            this.jumpRefs[this.jumpRefs.length] = id;
            id.sym = sym;
            id.cloId = this.addCloRef(id, null);
        }

        public buildControlFlow(): ControlFlowContext {
            var entry = new BasicBlock();
            var exit = new BasicBlock();

            var context = new ControlFlowContext(entry, exit);

            var controlFlowPrefix = (ast: AST, parent: AST, walker: IAstWalker) => {
                ast.addToControlFlow(walker.state);
                return ast;
            }

            var walker = getAstWalkerFactory().getWalker(controlFlowPrefix, null, null, context);
            context.walker = walker;
            walker.walk(this.bod, this);

            return context;
        }

        public typeCheck(typeFlow: TypeFlow) {
            return typeFlow.typeCheckFunction(this);
        }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            emitter.emitJavascriptFunction(this, writeDeclFile);
        }

        public getNameText() {
            if (this.name) {
                return this.name.text;
            }
            else {
                return this.hint;
            }
        }

        public isMethod() {
            return (this.fncFlags & FncFlags.Method) != FncFlags.None;
        }

        public isCallMember() { return hasFlag(this.fncFlags, FncFlags.CallMember); }
        public isConstructMember() { return hasFlag(this.fncFlags, FncFlags.ConstructMember); }
        public isIndexerMember() { return hasFlag(this.fncFlags, FncFlags.IndexerMember); }
        public isSpecialFn() { return this.isCallMember() || this.isIndexerMember() || this.isConstructMember(); }
        public isAnonymousFn() { return this.name === null; }
        public isAccessor() { return hasFlag(this.fncFlags, FncFlags.GetAccessor) || hasFlag(this.fncFlags, FncFlags.SetAccessor); }
        public isGetAccessor() { return hasFlag(this.fncFlags, FncFlags.GetAccessor); }
        public isSetAccessor() { return hasFlag(this.fncFlags, FncFlags.SetAccessor); }
        public isAmbient() { return hasFlag(this.fncFlags, FncFlags.Ambient); }
        public isExported() { return hasFlag(this.fncFlags, FncFlags.Exported); }
        public isPrivate() { return hasFlag(this.fncFlags, FncFlags.Private); }
        public isPublic() { return hasFlag(this.fncFlags, FncFlags.Public); }
        public isStatic() { return hasFlag(this.fncFlags, FncFlags.Static); }



        public treeViewLabel() {
            if (this.name == null) {
                return "funcExpr";
            }
            else {
                return "func: " + this.name.text
            }
        }

        public ClearFlags(): void {
            this.fncFlags = FncFlags.None;
        }

        public isSignature() { return (this.fncFlags & FncFlags.Signature) != FncFlags.None; }

        public hasStaticDeclarations() { return (!this.isConstructor && (this.statics.members.length > 0 || this.innerStaticFuncs.length > 0)); }
    }

    export class LocationInfo {
        constructor (public filename: string, public lineMap: number[], public unitIndex) { }
    }

    export var unknownLocationInfo = new LocationInfo("unknown", null, -1);

    export class Script extends FuncDecl {
        public locationInfo: LocationInfo = null;
        public requiresGlobal = false;
        public requiresInherits = false;
        public isResident = false;
        public isDeclareFile = false;
        public hasBeenTypeChecked = false;
        public topLevelMod: ModuleDecl = null;
        public leftCurlyCount = 0;
        public rightCurlyCount = 0;
        public vars: ASTList;
        public scopes: ASTList;

        constructor (vars: ASTList, scopes: ASTList) {
            super(new Identifier("script"), null, false, null, vars, scopes, null, NodeType.Script);
            this.vars = vars;
            this.scopes = scopes;
        }

        public typeCheck(typeFlow: TypeFlow) {
            return typeFlow.typeCheckScript(this);
        }

        public treeViewLabel() {
            return "Script";
        }

        public emitRequired() {
            if (!this.isDeclareFile && !this.isResident && this.bod) {
                for (var i = 0, len = this.bod.members.length; i < len; i++) {
                    var stmt = this.bod.members[i];
                    if (stmt.nodeType == NodeType.Module) {
                        if (!hasFlag((<ModuleDecl>stmt).modFlags, ModuleFlags.ShouldEmitModuleDecl | ModuleFlags.Ambient)) {
                            return true;
                        }
                    }
                    else if (stmt.nodeType == NodeType.Class) {
                        if (!hasFlag((<TypeDecl>stmt).varFlags, VarFlags.Ambient)) {
                            return true;
                        }
                    }
                    else if (stmt.nodeType == NodeType.VarDecl) {
                        if (!hasFlag((<VarDecl>stmt).varFlags, VarFlags.Ambient)) {
                            return true;
                        }
                    }
                    else if (stmt.nodeType == NodeType.FuncDecl) {
                        if (!(<FuncDecl>stmt).isSignature()) {
                            return true;
                        }
                    }
                    else if (stmt.nodeType != NodeType.Interface && stmt.nodeType != NodeType.Empty) {
                        return true;
                    }
                }
            }
            return false;
        }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            if (this.emitRequired()) {
                emitter.emitParensAndCommentsInPlace(this, true);
                emitter.recordSourceMappingStart(this);
                emitter.emitPrologue(this.requiresInherits);
                var oldDeclContainingAST: AST = writeDeclFile ? emitter.setDeclContainingAST(this) : null;
                emitter.emitJavascriptList(this.bod, null, TokenID.SColon, true, false, false, writeDeclFile);
                if (writeDeclFile) {
                    emitter.setDeclContainingAST(oldDeclContainingAST);
                }
                emitter.recordSourceMappingEnd(this);
                emitter.emitParensAndCommentsInPlace(this, false);
            }
        }

    }

    export class Record extends AST {
        public nty: NodeType;
        constructor (nty: number, public name: AST, public members: AST) {
            super(nty);
            this.nty = nty;
        }
    }

    export class ModuleDecl extends Record {

        public modFlags = ModuleFlags.ShouldEmitModuleDecl;
        public mod: ModuleType;
        public alias: AST = null;
        public leftCurlyCount = 0;
        public rightCurlyCount = 0;
        public prettyName: string;
        public amdDependencies: string[] = [];
        public members: ASTList;
        public vars: ASTList;
        public name: Identifier;
        public scopes: ASTList;

        constructor (name: Identifier, members: ASTList, vars: ASTList, scopes: ASTList) {
            super(NodeType.Module, name, members);

            this.members = members;
            this.vars = vars;
            this.name = name;
            this.scopes = scopes;

            this.prettyName = this.name.text;
        }

        public isExported() { return hasFlag(this.modFlags, ModuleFlags.Exported); }
        public isAmbient() { return hasFlag(this.modFlags, ModuleFlags.Ambient); }
        public isEnum() { return hasFlag(this.modFlags, ModuleFlags.IsEnum); }

        public recordNonInterface() {
            this.modFlags &= ~ModuleFlags.ShouldEmitModuleDecl;
        }

        public typeCheck(typeFlow: TypeFlow) {
            return typeFlow.typeCheckModule(this);
        }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            if (!hasFlag(this.modFlags, ModuleFlags.ShouldEmitModuleDecl)) {
                emitter.emitParensAndCommentsInPlace(this, true);
                emitter.emitJavascriptModule(this, writeDeclFile);
                emitter.emitParensAndCommentsInPlace(this, false);
            } else if (writeDeclFile) {
                emitter.emitModuleSignature(this);
            }
        }
    }

    export class NamedType extends Record {
        public name: Identifier;
        public members: AST;

        constructor (nty: NodeType, name: Identifier, public extendsList: ASTList, public implementsList: ASTList, members: AST) {
            super(nty, name, members);
            this.name = name;
            this.members = members;
        }
    }

    export class ClassDecl extends NamedType {
        public varFlags = VarFlags.None;
        public leftCurlyCount = 0;
        public rightCurlyCount = 0;
        public knownMemberNames: any = {};
        public constructorDecl: FuncDecl = null;
        public constructorNestingLevel = 0;
        public allMemberDefinitions: ASTList = new ASTList();
        public name: Identifier;
        public baseClass: ASTList;
        public implementsList: ASTList;
        public definitionMembers: ASTList;

        constructor (name: Identifier, definitionMembers: ASTList, baseClass: ASTList,
            implementsList: ASTList) {

            super(NodeType.Class, name, baseClass, implementsList, definitionMembers);

            this.name = name;
            this.baseClass = baseClass;
            this.implementsList = implementsList;
            this.definitionMembers = definitionMembers;
        }

        public isExported() { return hasFlag(this.varFlags, VarFlags.Exported); }
        public isAmbient() { return hasFlag(this.varFlags, VarFlags.Ambient); }

        public typeCheck(typeFlow: TypeFlow) {
            return typeFlow.typeCheckClass(this);
        }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            emitter.emitJavascriptClass(this, writeDeclFile);
        }
    }

    export class TypeDecl extends NamedType {
        public varFlags = VarFlags.None;
        public isOverload = false;
        public leftCurlyCount = 0;
        public rightCurlyCount = 0;
        public nty: NodeType;
        public name: Identifier;
        public extendsList: ASTList;
        public implementsList: ASTList;
        public members: AST;

        constructor (nty: NodeType, name: Identifier, members: AST, public args: ASTList, extendsList: ASTList,
            implementsList: ASTList) {
            super(nty, name, extendsList, implementsList, members);

            this.nty = nty;
            this.name = name;
            this.extendsList = extendsList;
            this.implementsList = implementsList;
            this.members = members;
        }

        public isExported() { return hasFlag(this.varFlags, VarFlags.Exported); }
        public isAmbient() { return hasFlag(this.varFlags, VarFlags.Ambient); }

        public typeCheck(typeFlow: TypeFlow) {
            if (this.nty == NodeType.Interface) {
                return typeFlow.typeCheckInterface(this);
            }
            else {
                throw new Error("please implement type check for node type" + this.nty);
            }
        }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            if (this.nty == NodeType.Interface) {
                if (writeDeclFile) {
                    emitter.emitInterfaceDeclaration(this);
                }
            }
            else {
                throw new Error("please implement emit for node type" + this.nty);
            }
        }
    }

    export class Statement extends AST {

        constructor (nty: number) {
            super(nty);
            this.flags |= ASTFlags.IsStatement;
        }
        public isLoop() { return false; }
        public isCompoundStatement() { return this.isLoop(); }
        public typeCheck(typeFlow: TypeFlow) {
            this.type = typeFlow.voidType;
            return this;
        }
    }

    export class LabeledStatement extends Statement {

        constructor (public labels: ASTList, public stmt: AST) {
            super(NodeType.LabeledStatement);
        }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            emitter.emitParensAndCommentsInPlace(this, true);
            emitter.recordSourceMappingStart(this);
            if (this.labels) {
                var labelsLen = this.labels.members.length;
                for (var i = 0; i < labelsLen; i++) {
                    this.labels.members[i].emit(emitter, tokenId, startLine, writeDeclFile);
                }
            }
            this.stmt.emit(emitter, tokenId, true, writeDeclFile);
            emitter.recordSourceMappingEnd(this);
            emitter.emitParensAndCommentsInPlace(this, false);
        }

        public typeCheck(typeFlow: TypeFlow) {
            typeFlow.typeCheck(this.labels);
            this.stmt = this.stmt.typeCheck(typeFlow);
            return this;
        }

        public addToControlFlow(context: ControlFlowContext): void {
            var beforeBB = context.current;
            var bb = new BasicBlock();
            context.current = bb;
            beforeBB.addSuccessor(bb);
        }
    }

    export class Block extends Statement {

        constructor (public stmts: ASTList, public visible: bool) {
            super(NodeType.Block);
        }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            emitter.emitParensAndCommentsInPlace(this, true);
            emitter.recordSourceMappingStart(this);
            if (this.visible) {
                emitter.writeLineToOutput(" {");
                emitter.increaseIndent();
            }
            var temp = emitter.setInObjectLiteral(false);
            if (this.stmts) {
                emitter.emitJavascriptList(this.stmts, null, TokenID.SColon, true, false, false);
            }
            if (this.visible) {
                emitter.decreaseIndent();
                emitter.emitIndent();
                emitter.writeToOutput("}");
            }
            emitter.setInObjectLiteral(temp);
            emitter.recordSourceMappingEnd(this);
            emitter.emitParensAndCommentsInPlace(this, false);
        }

        public addToControlFlow(context: ControlFlowContext) {
            var afterIfNeeded = new BasicBlock();
            context.pushStatement(this, context.current, afterIfNeeded);
            if (this.stmts) {
                context.walk(this.stmts, this);
            }
            context.walker.options.goChildren = false;
            context.popStatement();
            if (afterIfNeeded.predecessors.length > 0) {
                context.current.addSuccessor(afterIfNeeded);
                context.current = afterIfNeeded;
            }
        }

        public typeCheck(typeFlow: TypeFlow) {
            if (!typeFlow.checker.styleSettings.emptyBlocks) {
                if ((this.stmts === null) || (this.stmts.members.length == 0)) {
                    typeFlow.checker.errorReporter.styleError(this, "empty block");
                }
            }
            typeFlow.typeCheck(this.stmts);
            return this;
        }
    }

    export class Jump extends Statement {
        public target: string = null;
        public hasExplicitTarget() { return (this.target); }
        public resolvedTarget: Statement = null;
        public nty;

        constructor (nty: number) {
            super(nty);
            this.nty = nty;
        }

        public setResolvedTarget(parser: Parser, stmt: Statement): bool {
            if (stmt.isLoop()) {
                this.resolvedTarget = stmt;
                return true;
            }
            if (this.nty === NodeType.Continue) {
                parser.reportParseError("continue statement applies only to loops");
                return false;
            }
            else {
                if ((stmt.nodeType == NodeType.Switch) || this.hasExplicitTarget()) {
                    this.resolvedTarget = stmt;
                    return true;
                }
                else {
                    parser.reportParseError("break statement with no label can apply only to a loop or switch statement");
                    return false;
                }
            }
        }

        public addToControlFlow(context: ControlFlowContext): void {
            super.addToControlFlow(context);
            context.unconditionalBranch(this.resolvedTarget, (this.nty == NodeType.Continue));
        }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            emitter.emitParensAndCommentsInPlace(this, true);
            emitter.recordSourceMappingStart(this);
            if (this.nty == NodeType.Break) {
                emitter.writeToOutput("break");
            }
            else {
                emitter.writeToOutput("continue");
            }
            if (this.hasExplicitTarget()) {
                emitter.writeToOutput(" " + this.target);
            }
            emitter.writeToOutput(";");
            emitter.recordSourceMappingEnd(this);
            emitter.emitParensAndCommentsInPlace(this, false);
        }
    }

    export class WhileStatement extends Statement {
        public isStatementOrExpression() { return true; }
        public body: AST = null;

        constructor (public cond: AST) {
            super(NodeType.While);
        }

        public isLoop() { return true; }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            emitter.emitParensAndCommentsInPlace(this, true);
            emitter.recordSourceMappingStart(this);
            var temp = emitter.setInObjectLiteral(false);
            emitter.writeToOutput("while(");
            emitter.emitJavascript(this.cond, TokenID.WHILE, false);
            emitter.writeToOutput(")");
            emitter.emitJavascriptStatements(this.body, false, false);
            emitter.setInObjectLiteral(temp);
            emitter.recordSourceMappingEnd(this);
            emitter.emitParensAndCommentsInPlace(this, false);
        }

        public typeCheck(typeFlow: TypeFlow) {
            return typeFlow.typeCheckWhile(this);
        }

        public addToControlFlow(context: ControlFlowContext): void {
            var loopHeader = context.current;
            var loopStart = new BasicBlock();
            var afterLoop = new BasicBlock();

            loopHeader.addSuccessor(loopStart);
            context.current = loopStart;
            context.addContent(this.cond);
            var condBlock = context.current;
            var targetInfo: ITargetInfo = null;
            if (this.body) {
                context.current = new BasicBlock();
                condBlock.addSuccessor(context.current);
                context.pushStatement(this, loopStart, afterLoop);
                context.walk(this.body, this);
                targetInfo = context.popStatement();
            }
            if (!(context.noContinuation)) {
                var loopEnd = context.current;
                loopEnd.addSuccessor(loopStart);
            }
            context.current = afterLoop;
            condBlock.addSuccessor(afterLoop);
            // TODO: check for while (true) and then only continue if afterLoop has predecessors
            context.noContinuation = false;
            context.walker.options.goChildren = false;
        }
    }

    export class DoWhileStatement extends Statement {
        public isStatementOrExpression() { return true; }
        public body: AST = null;
        public whileAST: AST = null;
        public cond: AST = null;
        public isLoop() { return true; }

        constructor () {
            super(NodeType.DoWhile);
        }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            emitter.emitParensAndCommentsInPlace(this, true);
            emitter.recordSourceMappingStart(this);
            var temp = emitter.setInObjectLiteral(false);
            emitter.writeToOutput("do");
            emitter.emitJavascriptStatements(this.body, true, false);
            emitter.recordSourceMappingStart(this.whileAST);
            emitter.writeToOutput("while");
            emitter.recordSourceMappingEnd(this.whileAST);
            emitter.writeToOutput('(');
            emitter.emitJavascript(this.cond, TokenID.RParen, false);
            emitter.writeToOutput(")");
            emitter.setInObjectLiteral(temp);
            emitter.recordSourceMappingEnd(this);
            emitter.emitParensAndCommentsInPlace(this, false);
        }

        public typeCheck(typeFlow: TypeFlow) {
            return typeFlow.typeCheckDoWhile(this);
        }

        public addToControlFlow(context: ControlFlowContext): void {
            var loopHeader = context.current;
            var loopStart = new BasicBlock();
            var afterLoop = new BasicBlock();
            loopHeader.addSuccessor(loopStart);
            context.current = loopStart;
            var targetInfo: ITargetInfo = null;
            if (this.body) {
                context.pushStatement(this, loopStart, afterLoop);
                context.walk(this.body, this);
                targetInfo = context.popStatement();
            }
            if (!(context.noContinuation)) {
                var loopEnd = context.current;
                loopEnd.addSuccessor(loopStart);
                context.addContent(this.cond);
                // TODO: check for while (true) 
                context.current = afterLoop;
                loopEnd.addSuccessor(afterLoop);
            }
            else {
                context.addUnreachable(this.cond);
            }
            context.walker.options.goChildren = false;
        }
    }

    export class IfStatement extends Statement {
        public isStatementOrExpression() { return true; }
        public thenBod: AST;
        public elseBod: AST = null;

        constructor (public cond: AST) {
            super(NodeType.If);
        }

        public isCompoundStatement() { return true; }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            emitter.emitParensAndCommentsInPlace(this, true);
            emitter.recordSourceMappingStart(this);
            var temp = emitter.setInObjectLiteral(false);
            emitter.writeToOutput("if(");
            emitter.emitJavascript(this.cond, TokenID.IF, false);
            emitter.writeToOutput(")");
            emitter.emitJavascriptStatements(this.thenBod, true, false);
            if (this.elseBod) {
                emitter.writeToOutput(" else");
                emitter.emitJavascriptStatements(this.elseBod, true, true);
            }
            emitter.setInObjectLiteral(temp);
            emitter.recordSourceMappingEnd(this);
            emitter.emitParensAndCommentsInPlace(this, false);
        }

        public typeCheck(typeFlow: TypeFlow) {
            return typeFlow.typeCheckIf(this);
        }

        public addToControlFlow(context: ControlFlowContext): void {
            this.cond.addToControlFlow(context);
            var afterIf = new BasicBlock();
            var beforeIf = context.current;
            context.pushStatement(this, beforeIf, afterIf);
            var hasContinuation = false;
            context.current = new BasicBlock();
            beforeIf.addSuccessor(context.current);
            context.walk(this.thenBod, this);
            if (!context.noContinuation) {
                hasContinuation = true;
                context.current.addSuccessor(afterIf);
            }
            if (this.elseBod) {
                // current block will be thenBod
                context.current = new BasicBlock();
                context.noContinuation = false;
                beforeIf.addSuccessor(context.current);
                context.walk(this.elseBod, this);
                if (!context.noContinuation) {
                    hasContinuation = true;
                    context.current.addSuccessor(afterIf);
                }
                else {
                    // thenBod created continuation for if statement
                    if (hasContinuation) {
                        context.noContinuation = false;
                    }
                }
            }
            else {
                beforeIf.addSuccessor(afterIf);
                context.noContinuation = false;
                hasContinuation = true;
            }
            var targetInfo = context.popStatement();
            if (afterIf.predecessors.length > 0) {
                context.noContinuation = false;
                hasContinuation = true;
            }
            if (hasContinuation) {
                context.current = afterIf;
            }
            context.walker.options.goChildren = false;
        }
    }

    export class ReturnStatement extends Statement {
        public returnExpression: AST = null;

        constructor () {
            super(NodeType.Return);
        }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            emitter.emitParensAndCommentsInPlace(this, true);
            emitter.recordSourceMappingStart(this);
            var temp = emitter.setInObjectLiteral(false);
            if (this.returnExpression) {
                emitter.writeToOutput("return ");
                emitter.emitJavascript(this.returnExpression, TokenID.SColon, false);
            }
            else {
                emitter.writeToOutput("return;");
            }
            emitter.setInObjectLiteral(temp);
            emitter.recordSourceMappingEnd(this);
            emitter.emitParensAndCommentsInPlace(this, false);
        }

        public addToControlFlow(context: ControlFlowContext): void {
            super.addToControlFlow(context);
            context.returnStmt();
        }

        public typeCheck(typeFlow: TypeFlow) {
            return typeFlow.typeCheckReturn(this);
        }
    }

    export class EndCode extends AST {
        constructor () {
            super(NodeType.EndCode);
        }
    }

    export class ForInStatement extends Statement {
        constructor (public lval: AST, public obj: AST) {
            super(NodeType.ForIn);
            if (this.lval && (this.lval.nodeType == NodeType.VarDecl)) {
                (<BoundDecl>this.lval).varFlags |= VarFlags.AutoInit;
            }
        }
        public body: AST;
        public isStatementOrExpression() { return true; }
        public isLoop() { return true; }

        public isFiltered() {
            if (this.body) {
                var singleItem: AST = null;
                if (this.body.nodeType == NodeType.List) {
                    var stmts = <ASTList>this.body;
                    if (stmts.members.length == 1) {
                        singleItem = stmts.members[0];
                    }
                }
                else {
                    singleItem = this.body;
                }
                // match template for filtering 'own' properties from obj
                if (singleItem !== null) {
                    if (singleItem.nodeType == NodeType.Block) {
                        var block = <Block>singleItem;
                        if ((block.stmts !== null) && (block.stmts.members.length == 1)) {
                            singleItem = block.stmts.members[0];
                        }
                    }
                    if (singleItem.nodeType == NodeType.If) {
                        var cond = (<IfStatement>singleItem).cond;
                        if (cond.nodeType == NodeType.Call) {
                            var target = (<CallExpression>cond).target;
                            if (target.nodeType == NodeType.Dot) {
                                var binex = <BinaryExpression>target;
                                if ((binex.operand1.nodeType == NodeType.Name) &&
                                    (this.obj.nodeType == NodeType.Name) &&
                                    ((<Identifier>binex.operand1).text == (<Identifier>this.obj).text)) {
                                    var prop = <Identifier>binex.operand2;
                                    if (prop.text == "hasOwnProperty") {
                                        var args = (<CallExpression>cond).args;
                                        if ((args !== null) && (args.members.length == 1)) {
                                            var arg = args.members[0];
                                            if ((arg.nodeType == NodeType.Name) &&
                                                 (this.lval.nodeType == NodeType.Name)) {
                                                if (((<Identifier>this.lval).text) == (<Identifier>arg).text) {
                                                    return true;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return false;
        }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            emitter.emitParensAndCommentsInPlace(this, true);
            emitter.recordSourceMappingStart(this);
            var temp = emitter.setInObjectLiteral(false);
            emitter.writeToOutput("for(");
            emitter.emitJavascript(this.lval, TokenID.FOR, false);
            emitter.writeToOutput(" in ");
            emitter.emitJavascript(this.obj, TokenID.FOR, false);
            emitter.writeToOutput(")");
            emitter.emitJavascriptStatements(this.body, true, false);
            emitter.setInObjectLiteral(temp);
            emitter.recordSourceMappingEnd(this);
            emitter.emitParensAndCommentsInPlace(this, false);
        }

        public typeCheck(typeFlow: TypeFlow) {
            if (typeFlow.checker.styleSettings.forin) {
                if (!this.isFiltered()) {
                    typeFlow.checker.errorReporter.styleError(this, "no hasOwnProperty filter");
                }
            }
            return typeFlow.typeCheckForIn(this);
        }

        public addToControlFlow(context: ControlFlowContext): void {
            if (this.lval) {
                context.addContent(this.lval);
            }
            if (this.obj) {
                context.addContent(this.obj);
            }

            var loopHeader = context.current;
            var loopStart = new BasicBlock();
            var afterLoop = new BasicBlock();

            loopHeader.addSuccessor(loopStart);
            context.current = loopStart;
            if (this.body) {
                context.pushStatement(this, loopStart, afterLoop);
                context.walk(this.body, this);
                context.popStatement();
            }
            if (!(context.noContinuation)) {
                var loopEnd = context.current;
                loopEnd.addSuccessor(loopStart);
            }
            context.current = afterLoop;
            context.noContinuation = false;
            loopHeader.addSuccessor(afterLoop);
            context.walker.options.goChildren = false;
        }
    }

    export class ForStatement extends Statement {
        public cond: AST;
        public body: AST;
        public incr: AST;

        constructor (public init: AST) {
            super(NodeType.For);
        }

        public isStatementOrExpression() { return true; }
        public isLoop() { return true; }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            emitter.emitParensAndCommentsInPlace(this, true);
            emitter.recordSourceMappingStart(this);
            var temp = emitter.setInObjectLiteral(false);
            emitter.writeToOutput("for(");
            if (this.init) {
                if (this.init.nodeType != NodeType.List) {
                    emitter.emitJavascript(this.init, TokenID.FOR, false);
                }
                else {
                    emitter.emitForVarList(<ASTList>this.init);
                }
            }
            emitter.writeToOutput("; ");
            emitter.emitJavascript(this.cond, TokenID.FOR, false);
            emitter.writeToOutput("; ");
            emitter.emitJavascript(this.incr, TokenID.FOR, false);
            emitter.writeToOutput(")");
            emitter.emitJavascriptStatements(this.body, true, false);
            emitter.setInObjectLiteral(temp);
            emitter.recordSourceMappingEnd(this);
            emitter.emitParensAndCommentsInPlace(this, false);
        }

        public typeCheck(typeFlow: TypeFlow) {
            return typeFlow.typeCheckFor(this);
        }

        public addToControlFlow(context: ControlFlowContext): void {
            if (this.init) {
                context.addContent(this.init);
            }
            var loopHeader = context.current;
            var loopStart = new BasicBlock();
            var afterLoop = new BasicBlock();

            loopHeader.addSuccessor(loopStart);
            context.current = loopStart;
            var condBlock: BasicBlock = null;
            var continueTarget = loopStart;
            var incrBB: BasicBlock = null;
            if (this.incr) {
                incrBB = new BasicBlock();
                continueTarget = incrBB;
            }
            if (this.cond) {
                condBlock = context.current;
                context.addContent(this.cond);
                context.current = new BasicBlock();
                condBlock.addSuccessor(context.current);
            }
            var targetInfo: ITargetInfo = null;
            if (this.body) {
                context.pushStatement(this, continueTarget, afterLoop);
                context.walk(this.body, this);
                targetInfo = context.popStatement();
            }
            if (this.incr) {
                if (context.noContinuation) {
                    if (incrBB.predecessors.length == 0) {
                        context.addUnreachable(this.incr);
                    }
                }
                else {
                    context.current.addSuccessor(incrBB);
                    context.current = incrBB;
                    context.addContent(this.incr);
                }
            }
            var loopEnd = context.current;
            if (!(context.noContinuation)) {
                loopEnd.addSuccessor(loopStart);

            }
            if (condBlock) {
                condBlock.addSuccessor(afterLoop);
                context.noContinuation = false;
            }
            if (afterLoop.predecessors.length > 0) {
                context.noContinuation = false;
                context.current = afterLoop;
            }
            context.walker.options.goChildren = false;
        }
    }

    export class WithStatement extends Statement {
        public body: AST;
        public isStatementOrExpression() { return true; }
        public isCompoundStatement() { return true; }
        public withSym: WithSymbol = null;

        constructor (public expr: AST) {
            super(NodeType.With);
        }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            emitter.emitParensAndCommentsInPlace(this, true);
            emitter.recordSourceMappingStart(this);
            emitter.writeToOutput("with (");
            if (this.expr) {
                emitter.emitJavascript(this.expr, TokenID.WITH, false);
            }

            emitter.writeToOutput(")");
            emitter.emitJavascriptStatements(this.body, true, false);
            emitter.recordSourceMappingEnd(this);
            emitter.emitParensAndCommentsInPlace(this, false);
        }

        public typeCheck(typeFlow: TypeFlow) {
            return typeFlow.typeCheckWith(this);
        }
    }

    export class SwitchStatement extends Statement {
        public caseList: ASTList;
        public defaultCase: CaseStatement = null;

        constructor (public val: AST) {
            super(NodeType.Switch);
        }

        public isStatementOrExpression() { return true; }
        public isCompoundStatement() { return true; }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool) {
            emitter.emitParensAndCommentsInPlace(this, true);
            emitter.recordSourceMappingStart(this);
            var temp = emitter.setInObjectLiteral(false);
            emitter.writeToOutput("switch(");
            emitter.emitJavascript(this.val, TokenID.ID, false);
            emitter.writeLineToOutput(") {");
            emitter.increaseIndent();
            var casesLen = this.caseList.members.length;
            for (var i = 0; i < casesLen; i++) {
                var caseExpr = this.caseList.members[i];
                emitter.emitJavascript(caseExpr, TokenID.CASE, true);
                emitter.writeLineToOutput("");
            }
            emitter.decreaseIndent();
            emitter.emitIndent();
            emitter.writeToOutput("}");
            emitter.setInObjectLiteral(temp);
            emitter.recordSourceMappingEnd(this);
            emitter.emitParensAndCommentsInPlace(this, false);
        }

        public typeCheck(typeFlow: TypeFlow) {
            var len = this.caseList.members.length;
            this.val = typeFlow.typeCheck(this.val);
            for (var i = 0; i < len; i++) {
                this.caseList.members[i] = typeFlow.typeCheck(this.caseList.members[i]);
            }
            this.defaultCase = <CaseStatement>typeFlow.typeCheck(this.defaultCase);
            this.type = typeFlow.voidType;
            return this;
        }

        // if there are break statements that match this switch, then just link cond block with block after switch
        public addToControlFlow(context: ControlFlowContext) {
            var condBlock = context.current;
            context.addContent(this.val);
            var execBlock = new BasicBlock();
            var afterSwitch = new BasicBlock();

            condBlock.addSuccessor(execBlock);
            context.pushSwitch(execBlock);
            context.current = execBlock;
            context.pushStatement(this, execBlock, afterSwitch);
            context.walk(this.caseList, this);
            context.popSwitch();
            var targetInfo = context.popStatement();
            var hasCondContinuation = (this.defaultCase == null);
            if (this.defaultCase == null) {
                condBlock.addSuccessor(afterSwitch);
            }
            if (afterSwitch.predecessors.length > 0) {
                context.noContinuation = false;
                context.current = afterSwitch;
            }
            else {
                context.noContinuation = true;
            }
            context.walker.options.goChildren = false;
        }
    }

    export class CaseStatement extends Statement {
        public expr: AST = null;
        public body: ASTList;

        constructor () {
            super(NodeType.Case);
        }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            emitter.emitParensAndCommentsInPlace(this, true);
            emitter.recordSourceMappingStart(this);
            if (this.expr) {
                emitter.writeToOutput("case ");
                emitter.emitJavascript(this.expr, TokenID.ID, false);
            }
            else {
                emitter.writeToOutput("default");
            }
            emitter.writeToOutput(":");
            emitter.emitJavascriptStatements(this.body, false, false);
            emitter.recordSourceMappingEnd(this);
            emitter.emitParensAndCommentsInPlace(this, false);
        }

        public typeCheck(typeFlow: TypeFlow) {
            this.expr = typeFlow.typeCheck(this.expr);
            typeFlow.typeCheck(this.body);
            this.type = typeFlow.voidType;
            return this;
        }

        // TODO: more reasoning about unreachable cases (such as duplicate literals as case expressions)
        // for now, assume all cases are reachable, regardless of whether some cases fall through
        public addToControlFlow(context: ControlFlowContext) {
            var execBlock = new BasicBlock();
            var sw = context.currentSwitch[context.currentSwitch.length - 1];
            // TODO: fall-through from previous (+ to end of switch)
            if (this.expr) {
                var exprBlock = new BasicBlock();
                context.current = exprBlock;
                sw.addSuccessor(exprBlock);
                context.addContent(this.expr);
                exprBlock.addSuccessor(execBlock);
            }
            else {
                sw.addSuccessor(execBlock);
            }
            context.current = execBlock;
            if (this.body) {
                context.walk(this.body, this);
            }
            context.noContinuation = false;
            context.walker.options.goChildren = false;
        }
    }

    export class TypeReference extends AST {

        constructor (public term: AST, public arrayCount: number) {
            super(NodeType.TypeRef);
        }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            throw new Error("should not emit a type ref");
        }

        public typeCheck(typeFlow: TypeFlow) {
            var prevInTCTR = typeFlow.inTypeRefTypeCheck;
            typeFlow.inTypeRefTypeCheck = true;
            var typeLink = getTypeLink(this, typeFlow.checker, true);
            typeFlow.checker.resolveTypeLink(typeFlow.scope, typeLink, false);

            typeFlow.checkForVoidConstructor(typeLink.type, this);

            this.type = typeLink.type;

            // in error recovery cases, there may not be a term
            if (this.term) {
                this.term.type = this.type;
            }

            typeFlow.inTypeRefTypeCheck = prevInTCTR;
            return this;
        }
    }

    export class TryFinally extends Statement {

        constructor (public tryNode: AST, public finallyNode: AST) {
            super(NodeType.TryFinally);
        }

        public isStatementOrExpression() { return true; }
        public isCompoundStatement() { return true; }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool) {
            emitter.recordSourceMappingStart(this);
            emitter.emitJavascript(this.tryNode, TokenID.TRY, false);
            emitter.emitJavascript(this.finallyNode, TokenID.FINALLY, false);
            emitter.recordSourceMappingEnd(this);
        }

        public typeCheck(typeFlow: TypeFlow) {
            this.tryNode = typeFlow.typeCheck(this.tryNode);
            this.finallyNode = typeFlow.typeCheck(this.finallyNode);
            this.type = typeFlow.voidType;
            return this;
        }

        public addToControlFlow(context: ControlFlowContext) {
            var afterFinally = new BasicBlock();
            context.walk(this.tryNode, this);
            var finBlock = new BasicBlock();
            if (context.current) {
                context.current.addSuccessor(finBlock);
            }
            context.current = finBlock;
            context.pushStatement(this, null, afterFinally);
            context.walk(this.finallyNode, this);
            if (!context.noContinuation && context.current) {
                context.current.addSuccessor(afterFinally);
            }
            if (afterFinally.predecessors.length > 0) {
                context.current = afterFinally;
            }
            else {
                context.noContinuation = true;
            }
            context.popStatement();
            context.walker.options.goChildren = false;
        }
    }

    export class TryCatch extends Statement {

        constructor (public tryNode: AST, public catchNode: AST) {
            super(NodeType.TryCatch);
        }

        public isStatementOrExpression() { return true; }
        public isCompoundStatement() { return true; }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            emitter.emitParensAndCommentsInPlace(this, true);
            emitter.recordSourceMappingStart(this);
            emitter.emitJavascript(this.tryNode, TokenID.TRY, false);
            emitter.emitJavascript(this.catchNode, TokenID.CATCH, false);
            emitter.recordSourceMappingEnd(this);
            emitter.emitParensAndCommentsInPlace(this, false);
        }

        public addToControlFlow(context: ControlFlowContext) {
            var beforeTry = context.current;
            var tryBlock = new BasicBlock();
            beforeTry.addSuccessor(tryBlock);
            context.current = tryBlock;
            var afterTryCatch = new BasicBlock();
            context.pushStatement(this, null, afterTryCatch);
            context.walk(this.tryNode, this);
            if (!context.noContinuation) {
                if (context.current) {
                    context.current.addSuccessor(afterTryCatch);
                }
            }
            context.current = new BasicBlock();
            beforeTry.addSuccessor(context.current);
            context.walk(this.catchNode, this);
            context.popStatement();
            if (!context.noContinuation) {
                if (context.current) {
                    context.current.addSuccessor(afterTryCatch);
                }
            }
            context.current = afterTryCatch;
            context.walker.options.goChildren = false;
        }

        public typeCheck(typeFlow: TypeFlow) {
            this.tryNode = typeFlow.typeCheck(this.tryNode);
            this.catchNode = typeFlow.typeCheck(this.catchNode);
            this.type = typeFlow.voidType;
            return this;
        }
    }

    export class Try extends Statement {

        constructor (public body: AST) {
            super(NodeType.Try);
        }

        public isStatementOrExpression() { return true; }
        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            emitter.emitParensAndCommentsInPlace(this, true);
            emitter.recordSourceMappingStart(this);
            emitter.writeToOutput("try ");
            emitter.emitJavascript(this.body, TokenID.TRY, false);
            emitter.recordSourceMappingEnd(this);
            emitter.emitParensAndCommentsInPlace(this, false);
        }

        public typeCheck(typeFlow: TypeFlow) {
            this.body = typeFlow.typeCheck(this.body);
            return this;
        }

        public addToControlFlow(context: ControlFlowContext) {
            if (this.body) {
                context.walk(this.body, this);
            }
            context.walker.options.goChildren = false;
            context.noContinuation = false;
        }
    }

    export class Catch extends Statement {

        constructor (public param: VarDecl, public body: AST) {
            super(NodeType.Catch);
            if (this.param) {
                this.param.varFlags |= VarFlags.AutoInit;
            }
        }

        public containedScope: SymbolScope = null;

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            emitter.emitParensAndCommentsInPlace(this, true);
            emitter.recordSourceMappingStart(this);
            emitter.writeToOutput(" catch (");
            emitter.emitJavascript(this.param, TokenID.LParen, false);
            emitter.writeToOutput(")");
            emitter.emitJavascript(this.body, TokenID.CATCH, false);
            emitter.recordSourceMappingEnd(this);
            emitter.emitParensAndCommentsInPlace(this, false);
        }

        public addToControlFlow(context: ControlFlowContext) {
            if (this.param) {
                context.addContent(this.param);
                var bodBlock = new BasicBlock();
                context.current.addSuccessor(bodBlock);
                context.current = bodBlock;
            }
            if (this.body) {
                context.walk(this.body, this);
            }
            context.noContinuation = false;
            context.walker.options.goChildren = false;
        }

        public typeCheck(typeFlow: TypeFlow) {
            var prevScope = typeFlow.scope;
            typeFlow.scope = this.containedScope;
            this.param = <VarDecl>typeFlow.typeCheck(this.param);
            var exceptVar = new ValueLocation();
            var varSym = new VariableSymbol((<VarDecl>this.param).id.text,
                                          this.param.minChar,
                                          typeFlow.checker.locationInfo.unitIndex,
                                          exceptVar);
            exceptVar.symbol = varSym;
            exceptVar.typeLink = new TypeLink();
            // var type for now (add syntax for type annotation)
            exceptVar.typeLink.type = typeFlow.anyType;
            var thisFnc = typeFlow.thisFnc;
            if (thisFnc && thisFnc.type) {
                exceptVar.symbol.container = thisFnc.type.symbol;
            }
            else {
                exceptVar.symbol.container = null;
            }
            this.param.sym = exceptVar.symbol;
            typeFlow.scope.enter(exceptVar.symbol.container, this.param, exceptVar.symbol,
                                 typeFlow.checker.errorReporter, false, false, false);
            this.body = typeFlow.typeCheck(this.body);

            // if we're in provisional typecheck mode, clean up the symbol entry
            // REVIEW: This is obviously bad form, since we're counting on the internal
            // layout of the symbol table, but this is also the only place where we insert
            // symbols during typecheck
            if (typeFlow.checker.inProvisionalTypecheckMode()) {
                var table = typeFlow.scope.getTable();
                (<any>table).secondaryTable.table[exceptVar.symbol.name] = undefined;
            }
            this.type = typeFlow.voidType;
            typeFlow.scope = prevScope;
            return this;
        }
    }


    export class Finally extends Statement {

        constructor (public body: AST) {
            super(NodeType.Finally);
        }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            emitter.emitParensAndCommentsInPlace(this, true);
            emitter.recordSourceMappingStart(this);
            emitter.writeToOutput("finally");
            emitter.emitJavascript(this.body, TokenID.FINALLY, false);
            emitter.recordSourceMappingEnd(this);
            emitter.emitParensAndCommentsInPlace(this, false);
        }

        public addToControlFlow(context: ControlFlowContext) {
            if (this.body) {
                context.walk(this.body, this);
            }
            context.walker.options.goChildren = false;
            context.noContinuation = false;
        }

        public typeCheck(typeFlow: TypeFlow) {
            this.body = typeFlow.typeCheck(this.body);
            return this;
        }
    }

    export class Comment extends AST {

        public text: string[] = null;

        constructor (public content: string, public isBlockComment: bool, public endsLine) {
            super(NodeType.Comment);
        }

        public getText(): string[] {
            if (this.text == null) {
                if (this.isBlockComment) {
                    this.text = this.content.split("\n");
                    for (var i = 0; i < this.text.length; i++) {
                        this.text[i] = this.text[i].replace(/^\s+|\s+$/g, '');
                    }
                }
                else {
                    this.text = [(this.content.replace(/^\s+|\s+$/g, ''))];
                }
            }

            return this.text;
        }
    }

    export class DebuggerStatement extends Statement {
        constructor () {
            super(NodeType.Debugger);
        }

        public emit(emitter: Emitter, tokenId: TokenID, startLine: bool, writeDeclFile: bool) {
            emitter.emitParensAndCommentsInPlace(this, true);
            emitter.recordSourceMappingStart(this);
            emitter.writeLineToOutput("debugger;");
            emitter.recordSourceMappingEnd(this);
            emitter.emitParensAndCommentsInPlace(this, false);
        }
    }
}
