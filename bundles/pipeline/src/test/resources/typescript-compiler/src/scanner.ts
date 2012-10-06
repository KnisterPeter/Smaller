// Copyright (c) Microsoft. All rights reserved. Licensed under the Apache License, Version 2.0. 
// See LICENSE.txt in the project root for complete license information.

///<reference path='typescript.ts' />

module TypeScript {

    export var LexEOF = (-1);

    export var LexCodeNWL = 0x0A;
    export var LexCodeRET = 0x0D;
    export var LexCodeTAB = 0x09;
    export var LexCodeVTAB = 0x0B;
    export var LexCode_e = 'e'.charCodeAt(0);
    export var LexCode_E = 'E'.charCodeAt(0);
    export var LexCode_x = 'x'.charCodeAt(0);
    export var LexCode_X = 'X'.charCodeAt(0);
    export var LexCode_a = 'a'.charCodeAt(0);
    export var LexCode_A = 'A'.charCodeAt(0);
    export var LexCode_f = 'f'.charCodeAt(0);
    export var LexCode_F = 'F'.charCodeAt(0);

    export var LexCode_g = 'g'.charCodeAt(0);
    export var LexCode_m = 'm'.charCodeAt(0);
    export var LexCode_i = 'i'.charCodeAt(0);

    export var LexCode_0 = '0'.charCodeAt(0);
    export var LexCode_9 = '9'.charCodeAt(0);
    export var LexCode_8 = '8'.charCodeAt(0);
    export var LexCode_7 = '7'.charCodeAt(0);

    export var LexCodeBSL = '\\'.charCodeAt(0);
    export var LexCodeSHP = '#'.charCodeAt(0);
    export var LexCodeBNG = '!'.charCodeAt(0);
    export var LexCodeQUO = '"'.charCodeAt(0);
    export var LexCodeAPO = '\''.charCodeAt(0);
    export var LexCodePCT = '%'.charCodeAt(0);
    export var LexCodeAMP = '&'.charCodeAt(0);
    export var LexCodeLPR = '('.charCodeAt(0);
    export var LexCodeRPR = ')'.charCodeAt(0);
    export var LexCodePLS = '+'.charCodeAt(0);
    export var LexCodeMIN = '-'.charCodeAt(0);
    export var LexCodeMUL = '*'.charCodeAt(0);
    export var LexCodeSLH = '/'.charCodeAt(0);
    export var LexCodeXOR = '^'.charCodeAt(0);
    export var LexCodeCMA = ','.charCodeAt(0);
    export var LexCodeDOT = '.'.charCodeAt(0);
    export var LexCodeLT = '<'.charCodeAt(0);
    export var LexCodeEQ = '='.charCodeAt(0);
    export var LexCodeGT = '>'.charCodeAt(0);
    export var LexCodeQUE = '?'.charCodeAt(0);
    export var LexCodeLBR = '['.charCodeAt(0);
    export var LexCodeRBR = ']'.charCodeAt(0);
    export var LexCodeUSC = '_'.charCodeAt(0);
    export var LexCodeLC = '{'.charCodeAt(0);
    export var LexCodeRC = '}'.charCodeAt(0);
    export var LexCodeBAR = '|'.charCodeAt(0);
    export var LexCodeTIL = '~'.charCodeAt(0);
    export var LexCodeCOL = ':'.charCodeAt(0);
    export var LexCodeSMC = ';'.charCodeAt(0);
    export var LexCodeUnderscore = '_'.charCodeAt(0);
    export var LexCodeDollar = '$'.charCodeAt(0);
    export var LexCodeSpace = 32;

    export var LexKeywordTable = undefined;
    // TODO: use new Token[128];
    var autoToken: Token[] = new Array(128);
    var lexIdStartTable: bool[] = new Array(128);

    export function LexInitialize() {
        initializeStaticTokens();
        autoToken[LexCodeLPR] = staticTokens[TokenID.LParen];
        autoToken[LexCodeRPR] = staticTokens[TokenID.RParen];
        autoToken[LexCodeCMA] = staticTokens[TokenID.Comma];
        autoToken[LexCodeSMC] = staticTokens[TokenID.SColon];
        autoToken[LexCodeLBR] = staticTokens[TokenID.LBrack];
        autoToken[LexCodeRBR] = staticTokens[TokenID.RBrack];
        autoToken[LexCodeTIL] = staticTokens[TokenID.Tilde];
        autoToken[LexCodeQUE] = staticTokens[TokenID.QMark];
        autoToken[LexCodeLC] = staticTokens[TokenID.LCurly];
        autoToken[LexCodeRC] = staticTokens[TokenID.RCurly];
        autoToken[LexCodeCOL] = staticTokens[TokenID.Colon];
        LexKeywordTable = new StringHashTable();
        for (var i in (<any>TokenID)._map) {
            if ((<number><any>i) <= TokenID.LimKeyword) {
                LexKeywordTable.add((<any>TokenID)._map[i].toLowerCase(), i);
            }
        }
        for (var j = 0; j < 128; j++) {
            if (LexIsIdentifierStartChar(j)) {
                lexIdStartTable[j] = true;
            }
            else {
                lexIdStartTable[j] = false;
            }
        }
    }

    export function LexAdjustIndent(code, indentAmt) {
        if ((code == LexCodeLBR) || (code == LexCodeLC) || (code == LexCodeLPR)) {
            return indentAmt + 1;
        }
        else if ((code == LexCodeRBR) || (code == LexCodeRC) || (code == LexCodeRPR)) {
            return indentAmt - 1;
        }
        else return indentAmt;
    }

    export function LexIsIdentifierStartChar(code): bool {
        return (((code >= 97) && (code <= 122)) ||
                ((code >= 65) && (code <= 90)) ||
                (code == LexCodeDollar) ||
                (code == LexCodeUnderscore));
    }

    export function LexIsDigit(code): bool {
        return ((code >= 48) && (code <= 57));
    }

    export function LexIsIdentifierChar(code:number) {
        return lexIdStartTable[code] || LexIsDigit(code);
    }

    export function LexMatchingOpen(code) {
        if (code == LexCodeRBR)
            return LexCodeLBR;
        else if (code == LexCodeRC)
            return LexCodeLC;
        else if (code == LexCodeRPR)
            return LexCodeLPR;
        else return 0;
    }

    export enum NumberScanState {
        Start,
        InFraction,
        InExponent
    }

    export enum LexState {
        Start,
        InMultilineComment,
    }

    export enum LexMode {
        Line,
        File,
    }

    export enum CommentStyle {
        Line,
        Block
    }

    // Represent a piece of source code which can be read in multiple segments
    export interface ISourceText {
        getText(start: number, end: number): string;
        getLength(): number;
    }

    // Implementation on top of a contiguous string
    export class StringSourceText implements ISourceText {

        constructor (public text: string) { }

        public getText(start: number, end: number): string {
            return this.text.substring(start, end);
        }
        public getLength(): number {
            return this.text.length;
        }
    }

    export class SourceTextSegment implements ISourceTextSegment {
        constructor (public segmentStart: number,
                    public segmentEnd: number,
                    public segment: string) {
        }

        charCodeAt(index: number): number {
            return this.segment.charCodeAt(index - this.segmentStart);
        }

        substring(start: number, end: number): string {
            return this.segment.substring(start - this.segmentStart, end - this.segmentStart);
        }
    }

    export class AggerateSourceTextSegment implements ISourceTextSegment {

        constructor (public seg1: SourceTextSegment, public seg2: SourceTextSegment) { }

        public charCodeAt(index: number): number {
            if (this.seg1.segmentStart <= index && index < this.seg1.segmentEnd)
                return this.seg1.segment.charCodeAt(index - this.seg1.segmentStart);

            return this.seg2.segment.charCodeAt(index - this.seg2.segmentStart);
        }

        public substring(start: number, end: number): string {
            if (this.seg1.segmentStart <= start && end <= this.seg1.segmentEnd)
                return this.seg1.segment.substring(start - this.seg1.segmentStart, end - this.seg1.segmentStart);

            return this.seg2.segment.substring(start - this.seg2.segmentStart) + this.seg1.segment.substring(0, end - this.seg1.segmentStart);
        }
    }

    export interface ISourceTextSegment {
        charCodeAt(index: number): number;
        substring(start: number, end: number): string;
    }

    export class ScannerTextStream {
        static emptySegment = new SourceTextSegment(0, 0, "");
        public agg: AggerateSourceTextSegment;
        public len: number;

        constructor (public sourceText: ISourceText) {
            this.agg = new AggerateSourceTextSegment(ScannerTextStream.emptySegment, ScannerTextStream.emptySegment);
            this.len = this.sourceText.getLength();
        }

        public max(a: number, b: number): number {
            return a >= b ? a : b;
        }

        public min(a: number, b: number): number {
            return a <= b ? a : b;
        }

        public fetchSegment(start: number, end: number): ISourceTextSegment {
            // Common case
            if (this.agg.seg1.segmentStart <= start && end <= this.agg.seg1.segmentEnd)
                return this.agg.seg1;

            // Common overlap case
            if (this.agg.seg2.segmentStart <= start && end <= this.agg.seg1.segmentEnd)
                return this.agg;

            // if overlapping outside of fetched segment(s), fetch a new segment
            var prev = this.agg.seg1;

            var s = prev.segmentEnd;
            var e = max(s + 512, end); // ensure we move forward at least 512 characters or "end"
            e = min(e, this.len);    // but don't go past the end of the source text

            var src = this.sourceText.getText(s, e);
            var newSeg = new SourceTextSegment(s, e, src);
            this.agg.seg2 = prev;
            this.agg.seg1 = newSeg;
            return this.agg;
        }

        public charCodeAt(index: number): number {
            return this.fetchSegment(index, index + 1).charCodeAt(index);
        }

        public substring(start: number, end: number) {
            return this.fetchSegment(start, end).substring(start, end);
        }
    }

    export interface IScanner {
        startPos: number;
        pos: number;
        scan(): Token;
        previousToken(): Token;
        prevLine: number;
        line: number;
        col: number;
        leftCurlyCount: number;
        rightCurlyCount: number;
        lastTokenLimChar(): number;
        lastTokenHadNewline(): bool;
        lexState: number;
        getComments(): CommentToken[];
        getCommentsForLine(line: number): CommentToken[];
        resetComments(): void;
        lineMap: number[];
        setSourceText(newSrc: ISourceText, textMode: number): void;
    }

    export class SavedTokens implements IScanner {
        public prevToken: Token = null;
        public curSavedToken: SavedToken = null;
        public prevSavedToken: SavedToken = null;
        public currentTokenIndex: number;
        public currentTokens: SavedToken[];
        public tokensByLine: SavedToken[][];
        public lexStateByLine: LexState[];
        private prevToken: SavedToken = null;
        public previousToken(): Token { return this.prevToken; }
        public currentToken = 0;
        public tokens = new SavedToken[];
        public startPos: number;
        public pos: number;

        public close() {
            this.currentToken = 0;
        }

        public addToken(tok: Token, scanner: IScanner) {
            this.tokens[this.currentToken++] = new SavedToken(tok, scanner.startPos, scanner.pos);
        }

        public scan(): Token {
            // TODO: curly count
            this.startLine = this.line;
            this.startPos = this.col;
            if (this.currentTokenIndex == this.currentTokens.length) {
                if (this.line < this.lineMap.length) {
                    this.line++;
                    this.col = 0;
                    this.currentTokenIndex = 0;
                    this.currentTokens = this.tokensByLine[this.line];
                }
                else {
                    return staticTokens[TokenID.EOF];
                }
            }
            if (this.currentTokenIndex < this.currentTokens.length) {
                this.prevToken = this.curSavedToken.tok;
                this.prevSavedToken = this.curSavedToken;
                this.curSavedToken = this.currentTokens[this.currentTokenIndex++];
                var curToken = this.curSavedToken.tok;
                this.pos = this.curSavedToken.limChar;
                this.col += (this.curSavedToken.limChar - this.curSavedToken.minChar);
                this.startPos = this.curSavedToken.minChar;
                this.prevLine = this.line;
                return curToken;
            }
            else {
                return staticTokens[TokenID.EOF];
            }
        }
        public startLine: number;
        public prevLine = 1;
        public line = 1;
        public col = 0;
        public leftCurlyCount: number;
        public rightCurlyCount: number;

        public syncToTok(offset: number): number {
            this.line = getLineNumberFromPosition(this.lineMap, offset);
            this.currentTokenIndex = 0;
            var tmpCol = offset - this.lineMap[this.line];
            while ((this.lexStateByLine[this.line] == LexState.InMultilineComment) && (this.line > 0)) {
                this.line--;
                tmpCol = 0;
            }
            var lenMin1 = this.lineMap.length - 1;
            this.currentTokens = this.tokensByLine[this.line];
            while ((this.currentTokens.length == 0) && (this.line < lenMin1)) {
                this.line++;
                this.currentTokens = this.tokensByLine[this.line];
                tmpCol = 0;
            }
            if (this.line <= lenMin1) {
                while ((this.currentTokenIndex < this.currentTokens.length) &&
                       (tmpCol > this.currentTokens[this.currentTokenIndex].limChar)) {
                    this.currentTokenIndex++;
                }
                if (this.currentTokenIndex < this.currentTokens.length) {
                    this.col = this.currentTokens[this.currentTokenIndex].minChar;
                    return this.col + this.lineMap[this.line];
                }
            }
            return -1;
        }

        public lastTokenLimChar(): number {
            if (this.prevSavedToken !== null) {
                return this.prevSavedToken.limChar;
            }
            else {
                return 0;
            }
        }

        public lastTokenHadNewline(): bool {
            return this.prevLine != this.startLine;
        }

        public lexState = LexState.Start;

        public commentStack: CommentToken[] = new CommentToken[];

        public pushComment(comment: CommentToken) {
            this.commentStack.push(comment);
        }

        public getComments() {
            var stack = this.commentStack;
            this.commentStack = [];
            return stack;
        }

        public getCommentsForLine(line: number) {
            var comments: CommentToken[] = null;
            while ((this.commentStack.length > 0) && (this.commentStack[0].line == line)) {
                if (comments == null) {
                    comments = [this.commentStack.shift()];
                }
                else {
                    comments = comments.concat([this.commentStack.shift()]);
                }

            }
            return comments;
        }

        public resetComments() {
            this.commentStack = [];
        }

        public lineMap: number[];
        public setSourceText(newSrc: ISourceText, textMode: number) {
        }
    }

    export class Scanner implements IScanner {
        public prevLine = 1;
        public line = 1;
        public col = 0;
        public pos = 0;
        public startPos = 0;
        public startCol: number;
        public startLine: number;
        public src: string;
        public len = 0;
        public lineMap: number[] = [];
        
        public ch = LexEOF;
        public lexState = LexState.Start;
        public mode = LexMode.File;
        public scanComments: bool = true;
        public interveningWhitespace = false; // Was there a whitespace token between the last token and the current one?
        private interveningWhitespacePos = 0; //  If yes, this contains the start position of the whitespace
        public leftCurlyCount = 0;
        public rightCurlyCount = 0;
        public commentStack: CommentToken[] = new CommentToken[];
        public saveScan: SavedTokens = null;

        constructor () {
            this.startCol = this.col;
            this.startLine = this.line;            
            this.lineMap[1] = 0;
            
            if (!LexKeywordTable) {
                LexInitialize();
            }            
        }

        private prevTok = staticTokens[TokenID.EOF];
        public previousToken() { return this.prevTok; }

        public setSourceText(newSrc: ISourceText, textMode: number) {
            this.mode = textMode;
            this.scanComments = (this.mode === LexMode.Line);
            this.pos = 0;
            this.interveningWhitespacePos = 0;
            this.startPos = 0;
            this.line = 1;
            this.col = 0;
            this.startCol = this.col;
            this.startLine = this.line;
            this.len = 0;
            this.src = newSrc.getText(0, newSrc.getLength());
            this.len = this.src.length;
            this.lineMap = [];
            this.lineMap[1] = 0;
            this.commentStack = [];
            this.leftCurlyCount = 0;
            this.rightCurlyCount = 0;
        }

        public setSaveScan(savedTokens: SavedTokens) {
            this.saveScan = savedTokens;
        }

        public setText(newSrc: string, textMode: number) {
            this.setSourceText(new StringSourceText(newSrc), textMode);
        }

        public setScanComments(value: bool) {
            this.scanComments = value;
        }

        public getLexState(): number {
            return this.lexState;
        }

        public scanLine(line: string, initialState: number): Token[] {
            this.lexState = initialState;
            var result: Token[] = new Token[];
            this.setText(line, LexMode.Line);
            var t: Token = this.scan();
            while (t.tokenId != TokenID.EOF) {
                result[result.length] = t;
                t = this.scan();
            }
            return result;
        }

        public tokenStart() {
            this.startPos = this.pos;
            this.startLine = this.line;
            this.startCol = this.col;
            this.interveningWhitespace = false;
        }

        public peekChar(): number {
            if (this.pos < this.len) {
                return this.src.charCodeAt(this.pos);
            }
            else {
                return LexEOF;
            }
        }

        public peekCharAt(index: number): number {
            if (index < this.len) {
                return this.src.charCodeAt(index);
            }
            else {
                return LexEOF;
            }
        }

        public IsHexDigit(c: number) {
            return ((c >= LexCode_0) && (c <= LexCode_9)) || ((c >= LexCode_A) && (c <= LexCode_F)) ||
                ((c >= LexCode_a) && (c <= LexCode_f));
        }

        public IsOctalDigit(c: number) {
            return ((c >= LexCode_0) && (c <= LexCode_7)) ||
                ((c >= LexCode_a) && (c <= LexCode_f));
        }

        public scanHexDigits(): Token {
            var atLeastOneDigit = false;
            for (; ;) {
                if (this.IsHexDigit(this.ch)) {
                    this.nextChar();
                    atLeastOneDigit = true;
                }
                else {
                    if (atLeastOneDigit) {
                        return new NumberToken(parseInt(this.src.substring(this.startPos, this.pos)));
                    }
                    else {
                        return null;
                    }
                }
            }

        }

        public scanOctalDigits(): Token {
            var atLeastOneDigit = false;
            for (; ;) {
                if (this.IsOctalDigit(this.ch)) {
                    this.nextChar();
                    atLeastOneDigit = true;
                }
                else {
                    if (atLeastOneDigit) {
                        return new NumberToken(parseInt(this.src.substring(this.startPos, this.pos)));
                    }
                    else {
                        return null;
                    }
                }
            }

        }

        public scanDecimalNumber(state: number): Token {
            var atLeastOneDigit = false;
            var svPos = this.pos;
            var svCol = this.col;
            for (; ;) {
                if (LexIsDigit(this.ch)) {
                    atLeastOneDigit = true;
                    this.nextChar();
                }
                else if (this.ch == LexCodeDOT) {
                    if (state == NumberScanState.Start) {
                        // DecimalDigit* .
                        this.nextChar();
                        state = NumberScanState.InFraction;
                    }
                    else {
                        // dot not part of number
                        if (atLeastOneDigit) {
                            // DecimalDigit* . DecimalDigit+
                            return new NumberToken(parseFloat(this.src.substring(this.startPos, this.pos)));
                        }
                        else {
                            this.pos = svPos;
                            this.col = svCol;
                            return null;
                        }
                    }
                } else if ((this.ch == LexCode_e) || (this.ch == LexCode_E)) {
                    if (state == NumberScanState.Start) {
                        if (atLeastOneDigit) {
                            // DecimalDigit+ (. DecimalDigit+) [eE] [+-]DecimalDigit+
                            atLeastOneDigit = false;
                            this.nextChar();
                            state = NumberScanState.InExponent;
                        }
                        else {
                            this.pos = svPos;
                            this.col = svCol;
                            return null;
                        }
                    }
                    else if (state == NumberScanState.InFraction) {
                        // DecimalDigit+ . DecimalDigit* [eE]
                        this.nextChar();
                        state = NumberScanState.InExponent;
                        atLeastOneDigit = false;
                    }
                    else {
                        // DecimalDigit+ . DecimalDigit* [eE] DecimalDigit+
                        if (atLeastOneDigit) {
                            return new NumberToken(parseFloat(this.src.substring(this.startPos, this.pos)));
                        }
                        else {
                            this.pos = svPos;
                            this.col = svCol;
                            return null;
                        }
                    }
                }
                else if ((this.ch == LexCodePLS) || (this.ch == LexCodeMIN)) {
                    if (state == NumberScanState.InExponent) {
                        if (!atLeastOneDigit) {
                            this.nextChar();
                        }
                        else {
                            this.pos = svPos;
                            this.col = svCol;
                            return null;
                        }
                    }
                    else if (state == NumberScanState.InFraction) {
                        return new NumberToken(parseFloat(this.src.substring(this.startPos, this.pos)));
                    }
                    else {
                        if (!atLeastOneDigit) {
                            this.pos = svPos;
                            this.col = svCol;
                            return null;
                        }
                        else {
                            return new NumberToken(parseFloat(this.src.substring(this.startPos, this.pos)));
                        }
                    }
                }
                else {
                    if (!atLeastOneDigit) {
                        this.pos = svPos;
                        this.col = svCol;
                        return null;
                    }
                    else {
                        return new NumberToken(parseFloat(this.src.substring(this.startPos, this.pos)));
                    }
                }
            }
        }

        // 0 [xX] hexDigits
        // 0 octalDigits
        // 0 [89] decimalDigits
        // decimalDigits? fraction? exponent?

        public scanNumber(): Token {
            if (this.peekChar() == LexCode_0) {
                switch (this.peekCharAt(this.pos + 1)) {
                    case LexCode_x:
                    case LexCode_X:
                        // Hex
                        this.advanceChar(2);
                        return this.scanHexDigits();
                    case LexCode_8:
                    case LexCode_9:
                    case LexCodeDOT:
                        return this.scanDecimalNumber(NumberScanState.Start);
                    default:
                        // Octal
                        return this.scanOctalDigits();
                }
            }
            else {
                return this.scanDecimalNumber(NumberScanState.Start);
            }
        }

        public scanFraction(): Token {
            return this.scanDecimalNumber(NumberScanState.InFraction);
        }

        public newLine() {
            this.col = 0;
            if (this.mode == LexMode.File) {
                this.line++;
                this.lineMap[this.line] = this.pos + 1;
            }
        }

        public finishMultilineComment(): bool {
            var ch2: number;
            this.lexState = LexState.InMultilineComment;
            while (this.pos < this.len) {
                if (this.ch == LexCodeMUL) {
                    ch2 = this.peekCharAt(this.pos + 1);
                    if (ch2 == LexCodeSLH) {
                        this.advanceChar(2);
                        if (this.mode == LexMode.File) {
                            this.tokenStart();
                        }
                        this.lexState = LexState.Start;
                        return true;
                    }
                }
                else if (this.ch == LexCodeNWL) {
                    this.newLine();
                    if (this.mode == LexMode.Line) {
                        this.nextChar();
                        return false;
                    }
                }
                this.nextChar();
            }
            return false;
        }

        public pushComment(comment: CommentToken) {
            this.commentStack.push(comment);
        }

        public getComments() {
            var stack = this.commentStack;
            this.commentStack = [];
            return stack;
        }

        public getCommentsForLine(line: number) {
            var comments: CommentToken[] = null;
            while ((this.commentStack.length > 0) && (this.commentStack[0].line == line)) {
                if (comments == null) {
                    comments = [this.commentStack.shift()];
                }
                else {
                    comments = comments.concat([this.commentStack.shift()]);
                }

            }
            return comments;
        }

        public resetComments() {
            this.commentStack = [];
        }

        public endsLine(c: number) {
            return (c == LexCodeNWL) || (c == LexCodeRET) || (c == 0x2028) || (c == 0x2029);
        }

        public finishSinglelineComment() {
            while (this.pos < this.len) {
                if (this.endsLine(this.ch))
                    break;
                this.nextChar();
            }

            if (this.mode == LexMode.File) {
                this.tokenStart();
            }
        }

        public tokenText(): string {
            return this.src.substring(this.startPos, this.pos);
        }

        public findClosingSLH() {
            var index = this.pos;
            var ch2 = this.src.charCodeAt(index);
            var prevCh = 0;
            var liveEsc = false;
            while (!this.endsLine(ch2) && (index < this.len)) {
                if ((ch2 == LexCodeSLH) && (!liveEsc)) {
                    return index;
                }
                prevCh = ch2;
                index++;
                if (liveEsc) {
                    liveEsc = false;
                }
                else {
                    liveEsc = (prevCh == LexCodeBSL);
                }

                ch2 = this.src.charCodeAt(index);
            }
            return -1;
        }

        public speculateRegex(): Token {
            if (noRegexTable[this.prevTok.tokenId] != undefined) {
                return null;
            }
            var svPos = this.pos;
            var svCol = this.col;
            // first char is '/' and has been skipped
            var index = this.findClosingSLH();
            if (index > 0) {
                // found closing /
                var pattern = this.src.substring(svPos, index);
                var flags = "";
                this.pos = index + 1;
                this.ch = this.peekChar();
                var flagsStart = this.pos;
                // TODO: check for duplicate flags
                while ((this.ch == LexCode_i) || (this.ch == LexCode_g) || (this.ch == LexCode_m)) {
                    this.nextChar();
                }
                if ((this.pos - flagsStart) > 3) {
                    return null;
                }
                else {
                    flags = this.src.substring(flagsStart, this.pos);
                }
                var regex = undefined;
                try {
                    regex = new RegExp(pattern, flags);
                }
                catch (regexException) {
                }
                if (regex) {
                    // no line boundary in regex string
                    this.col = svCol + (this.pos - this.startPos);
                    return new RegexToken(regex);
                }
            }
            this.pos = svPos;
            this.col = svCol;
            return null;
        }

        public lastTokenHadNewline() {
            return this.prevLine != this.startLine;
        }

        public lastTokenLimChar() {
            return this.interveningWhitespace ? this.interveningWhitespacePos : this.startPos;
        }

        // use only when known not to skip line terminators
        public advanceChar(amt: number) {
            this.pos += amt;
            this.col += amt;
            this.ch = this.peekChar();
        }

        public nextChar() {
            this.pos++;
            this.col++;
            this.ch = this.peekChar();
        }

        public scan(): Token {
            if ((this.lexState == LexState.InMultilineComment) && (this.scanComments)) {
                this.ch = this.peekChar();
                var commentLine = this.line;
                this.finishMultilineComment();
                if (this.startPos < this.pos) {
                    var commentText = this.src.substring(this.startPos, this.pos);
                    this.tokenStart();
                    return new CommentToken(TokenID.Comment, commentText,/*isBlock*/true, this.startPos, commentLine,/*endsLine*/true);
                }
                else {
                    return staticTokens[TokenID.EOF];
                }
            }
            this.prevLine = this.line;
            this.prevTok = this.innerScan();
            if (this.saveScan) {
                this.saveScan.addToken(this.prevTok, this);
            }
            return this.prevTok;
        }

        public innerScan(): Token {
            var rtok;
            this.tokenStart();
            this.ch = this.peekChar();

            while (this.pos < this.len) {
                if (lexIdStartTable[this.ch]) {
                    // identifier or keyword (TODO: Unicode letters)
                    do {
                        this.nextChar();
                    } while (lexIdStartTable[this.ch] || LexIsDigit(this.ch));
                    var idText = this.src.substring(this.startPos, this.pos);
                    var id:number;
                    if ((id = LexKeywordTable.lookup(idText)) != null) {
                        return staticTokens[id];
                    }
                    else {
                        return new StringToken(TokenID.ID, idText);
                    }
                }
                else if (this.ch == LexCodeSpace) {
                    if (!this.interveningWhitespace) {
                        this.interveningWhitespacePos = this.pos;
                    }
                    do {
                        this.nextChar();
                    } while (this.ch == LexCodeSpace);
                    if (this.mode == LexMode.Line) {
                        var whitespaceText = this.src.substring(this.startPos, this.pos);
                        return new WhitespaceToken(TokenID.Whitespace, whitespaceText);
                    }
                    else {
                        this.tokenStart();
                        this.interveningWhitespace = true;
                    }
                }
                else if (this.ch == LexCodeSLH) {
                    this.nextChar();
                    var commentText;
                    if (this.ch == LexCodeSLH) {
                        if (!this.interveningWhitespace) {
                            this.interveningWhitespacePos = this.pos - 1;
                        }
                        var commentStartPos = this.pos - 1;
                        var commentStartLine = this.line;
                        this.finishSinglelineComment();
                        var commentText = this.src.substring(commentStartPos, this.pos);
                        var commentToken = new CommentToken(TokenID.Comment, commentText,/*isBlock*/false, commentStartPos, commentStartLine,/*endsLine*/false);
                        if (this.scanComments) {
                            // respect scanner contract: when returning a token, startPos is the start position of the token
                            this.startPos = commentStartPos;
                            return commentToken;
                        }
                        else {
                            this.pushComment(commentToken);
                        }

                        this.interveningWhitespace = true;
                    }
                    else if (this.ch == LexCodeMUL) {
                        if (!this.interveningWhitespace) {
                            this.interveningWhitespacePos = this.pos - 1;
                        }
                        var commentStartPos = this.pos - 1;
                        var commentStartLine = this.line;
                        this.nextChar();  // Skip the "*"
                        this.finishMultilineComment();
                        var commentText = this.src.substring(commentStartPos, this.pos);
                        var endsLine = this.peekChar() == LexCodeNWL || this.peekChar() == LexCodeRET;
                        var commentToken = new CommentToken(TokenID.Comment, commentText,/*isBlock*/true, commentStartPos, commentStartLine, endsLine);
                        if (this.scanComments) {
                            // respect scanner contract: when returning a token, startPos is the start position of the token
                            this.startPos = commentStartPos;
                            return commentToken;
                        }
                        else {
                            this.pushComment(commentToken);
                        }
                        this.interveningWhitespace = true;
                    }
                    else {
                        var regexTok = this.speculateRegex();
                        if (regexTok) {
                            return regexTok;
                        }
                        else {
                            if (this.peekCharAt(this.pos) == LexCodeEQ) {
                                this.nextChar();
                                return staticTokens[TokenID.AsgDiv];
                            }
                            else {
                                return staticTokens[TokenID.Div];
                            }
                        }
                    }
                }
                else if (this.ch == LexCodeSMC) {
                    this.nextChar();
                    return staticTokens[TokenID.SColon];
                }
                else if ((this.ch == LexCodeAPO) || (this.ch == LexCodeQUO)) {
                    var endCode = this.ch;
                    var prevCh = 0;
                    // accumulate with escape characters; convert to unescaped string
                    // where necessary
                    var liveEsc = false;
                    do {
                        prevCh = this.ch;
                        if (liveEsc) {
                            liveEsc = false;
                        }
                        else {
                            liveEsc = (prevCh == LexCodeBSL);
                        }
                        this.nextChar();
                    } while ((this.ch != LexEOF) && (liveEsc || (this.ch != endCode)));

                    if (this.ch != LexEOF) {
                        // skip past end code
                        this.nextChar();
                    }
                    return new StringToken(TokenID.QString, this.src.substring(this.startPos, this.pos));
                }
                else if (autoToken[this.ch]) {
                    var atok = autoToken[this.ch];
                    if (atok.tokenId == TokenID.LCurly) {
                        this.leftCurlyCount++;
                    }
                    else if (atok.tokenId == TokenID.RCurly) {
                        this.rightCurlyCount++;
                    }
                    this.nextChar();
                    return atok;
                }
                else if ((this.ch >= LexCode_0) && (this.ch <= LexCode_9)) {
                    rtok = this.scanNumber();
                    if (rtok) {
                        return rtok;
                    }
                    else {
                        this.nextChar();
                        return staticTokens[TokenID.Error];
                    }
                }
                else switch (this.ch) {
                    // TAB
                    case LexCodeTAB:
                    case LexCodeVTAB:
                        if (!this.interveningWhitespace) {
                            this.interveningWhitespacePos = this.pos;
                        }
                        if (this.mode == LexMode.Line) {
                            do {
                                this.nextChar();
                            } while ((this.ch == LexCodeSpace) || (this.ch == 9));
                            var wsText = this.src.substring(this.startPos, this.pos);
                            return new WhitespaceToken(TokenID.Whitespace, wsText);
                        }
                        else {
                            this.interveningWhitespace = true;
                        }
                    // Newlines and BOM
                    case 0xFF: // UTF16 SEQUENCE
                    case 0xFE:
                    case 0xEF:    // UTF8 SEQUENCE
                    case 0xBB:
                    case 0xBF:
                    case 0x2028:
                    case 0x2029:
                    case LexCodeNWL:
                    case LexCodeRET:
                        if (this.ch == LexCodeNWL) {
                            this.newLine();
                            if (this.mode == LexMode.Line) {
                                return staticTokens[TokenID.EOF];
                            }
                        }
                        if (!this.interveningWhitespace) {
                            this.interveningWhitespacePos = this.pos;
                        }
                        this.nextChar();
                        this.tokenStart();
                        this.interveningWhitespace = true;
                        break;
                    case LexCodeDOT: {
                        if (this.peekCharAt(this.pos + 1) == LexCodeDOT) {
                            if (this.peekCharAt(this.pos + 2) == LexCodeDOT) {
                                this.advanceChar(3);
                                return staticTokens[TokenID.Ellipsis];
                            }
                            else {
                                this.nextChar();
                                return staticTokens[TokenID.Dot];
                            }
                        }
                        else {
                            this.nextChar();
                            rtok = this.scanFraction();
                            if (rtok) {
                                return rtok;
                            }
                            else {
                                return staticTokens[TokenID.Dot];
                            }
                        }
                        // break;
                    }
                    case LexCodeEQ:
                        if (this.peekCharAt(this.pos + 1) == LexCodeEQ) {
                            if (this.peekCharAt(this.pos + 2) == LexCodeEQ) {
                                this.advanceChar(3);
                                return staticTokens[TokenID.Eqv];
                            }
                            else {
                                this.advanceChar(2);
                                return staticTokens[TokenID.EQ];
                            }
                        }
                        else if (this.peekCharAt(this.pos + 1) == LexCodeGT) {
                            this.advanceChar(2);
                            return staticTokens[TokenID.Arrow];
                        }
                        else {
                            this.nextChar();
                            return staticTokens[TokenID.Asg];
                        }
                    // break;
                    case LexCodeBNG:
                        if (this.peekCharAt(this.pos + 1) == LexCodeEQ) {
                            if (this.peekCharAt(this.pos + 2) == LexCodeEQ) {
                                this.advanceChar(3);
                                return staticTokens[TokenID.NEqv];
                            }
                            else {
                                this.advanceChar(2);
                                return staticTokens[TokenID.NE];
                            }
                        }
                        else {
                            this.nextChar();
                            return staticTokens[TokenID.Bang];
                        }
                    // break;
                    case LexCodePLS:
                        if (this.peekCharAt(this.pos + 1) == LexCodeEQ) {
                            this.advanceChar(2);
                            return staticTokens[TokenID.AsgAdd];
                        }
                        else if (this.peekCharAt(this.pos + 1) == LexCodePLS) {
                            this.advanceChar(2);
                            return staticTokens[TokenID.Inc];
                        }
                        else {
                            this.nextChar();
                            return staticTokens[TokenID.Add];
                        }
                    // break;
                    case LexCodeMIN:
                        if (this.peekCharAt(this.pos + 1) == LexCodeEQ) {
                            this.advanceChar(2);
                            return staticTokens[TokenID.AsgSub];
                        }
                        else if (this.peekCharAt(this.pos + 1) == LexCodeMIN) {
                            this.advanceChar(2);
                            return staticTokens[TokenID.Dec];
                        }
                        else {
                            this.nextChar();
                            return staticTokens[TokenID.Sub];
                        }
                    // break;
                    case LexCodeMUL:
                        if (this.peekCharAt(this.pos + 1) == LexCodeEQ) {
                            this.advanceChar(2);
                            return staticTokens[TokenID.AsgMul];
                        }
                        else {
                            this.nextChar();
                            return staticTokens[TokenID.Mult];
                        }
                    // break;
                    case LexCodePCT:
                        if (this.peekCharAt(this.pos + 1) == LexCodeEQ) {
                            this.advanceChar(2);
                            return staticTokens[TokenID.AsgMod];
                        }
                        else {
                            this.nextChar();
                            return staticTokens[TokenID.Pct];
                        }
                    // break;
                    case LexCodeLT:
                        if (this.peekCharAt(this.pos + 1) == LexCodeLT) {
                            if (this.peekCharAt(this.pos + 2) == LexCodeEQ) {
                                this.advanceChar(3);
                                return staticTokens[TokenID.AsgLsh];
                            }
                            else {
                                this.advanceChar(2);
                                return staticTokens[TokenID.Lsh];
                            }
                        }
                        else if (this.peekCharAt(this.pos + 1) == LexCodeEQ) {
                            this.advanceChar(2);
                            return staticTokens[TokenID.LE];
                        }
                        else {
                            this.nextChar();
                            return staticTokens[TokenID.LT];
                        }
                    //  break;
                    case LexCodeGT:
                        if (this.peekCharAt(this.pos + 1) == LexCodeGT) {
                            if (this.peekCharAt(this.pos + 2) == LexCodeEQ) {
                                this.advanceChar(3);
                                return staticTokens[TokenID.AsgRsh];
                            }
                            else if (this.peekCharAt(this.pos + 2) == LexCodeGT) {
                                if (this.peekCharAt(this.pos + 3) == LexCodeEQ) {
                                    this.advanceChar(4);
                                    return staticTokens[TokenID.AsgRs2];
                                }
                                else {
                                    this.advanceChar(3);
                                    return staticTokens[TokenID.Rs2];
                                }
                            }
                            else {
                                this.advanceChar(2);
                                return staticTokens[TokenID.Rsh];
                            }
                        }
                        else if (this.peekCharAt(this.pos + 1) == LexCodeEQ) {
                            this.advanceChar(2);
                            return staticTokens[TokenID.GE];
                        }
                        else {
                            this.nextChar();
                            return staticTokens[TokenID.GT];
                        }
                    // break;
                    case LexCodeXOR:
                        if (this.peekCharAt(this.pos + 1) == LexCodeEQ) {
                            this.advanceChar(2);
                            return staticTokens[TokenID.AsgXor];
                        }
                        else {
                            this.nextChar();
                            return staticTokens[TokenID.Xor];
                        }
                    //  break;
                    case LexCodeBAR:
                        if (this.peekCharAt(this.pos + 1) == LexCodeEQ) {
                            this.advanceChar(2);
                            return staticTokens[TokenID.AsgOr];
                        }
                        else if (this.peekCharAt(this.pos + 1) == LexCodeBAR) {
                            this.advanceChar(2);
                            return staticTokens[TokenID.LogOr];
                        }
                        else {
                            this.nextChar();
                            return staticTokens[TokenID.Or];
                        }
                    //  break;
                    case LexCodeAMP:
                        if (this.peekCharAt(this.pos + 1) == LexCodeEQ) {
                            this.advanceChar(2);
                            return staticTokens[TokenID.AsgAnd];
                        }
                        else if (this.peekCharAt(this.pos + 1) == LexCodeAMP) {
                            this.advanceChar(2);
                            return staticTokens[TokenID.LogAnd];
                        }
                        else {
                            this.nextChar();
                            return staticTokens[TokenID.And];
                        }
                    //  break;
                    default:
                        // TODO:report error
                        return staticTokens[TokenID.EOF];
                }
            }
            return staticTokens[TokenID.EOF];
        }
    }

    // Reseverved words only apply to Identifiers, not IdentifierNames
    export function convertTokToIDName(tok: Token): bool {
        return convertTokToIDBase(tok, true, false);
    }

    export function convertTokToID(tok: Token, strictMode: bool): bool {
        return convertTokToIDBase(tok, false, strictMode);
    }

    function convertTokToIDBase(tok: Token, identifierName: bool, strictMode: bool): bool {
        if (tok.tokenId <= TokenID.LimKeyword) {
            var tokInfo = lookupToken(tok.tokenId);
            if (tokInfo != undefined) {
                var resFlags = Reservation.Javascript | Reservation.JavascriptFuture;
                if (strictMode) {
                    resFlags |= Reservation.JavascriptFutureStrict;
                }
                if (identifierName || !hasFlag(tokInfo.reservation, resFlags)) {
                    return true;
                }
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    // Return the (1-based) line number from a character offset using the provided linemap.
    export function getLineNumberFromPosition(lineMap: number[], position: number): number {
        if (position === -1)
            return 0;

        // Binary search
        var min = 0;
        var max = lineMap.length - 1;
        while (min < max) {
            var med = (min + max) >> 1;
            if (position < lineMap[med]) {
                max = med - 1;
            }
            else if (position < lineMap[med + 1]) {
                min = max = med; // found it
            }
            else {
                min = med + 1;
            }
        }

        return min;
    }

    /// Return the [line, column] data for a given offset and a lineMap.
    /// Note that the returned line is 1-based, while the column is 0-based.
    export function getSourceLineColFromMap(lineCol: ILineCol, minChar: number, lineMap: number[]): void {
        var line = getLineNumberFromPosition(lineMap, minChar);

        if (line > 0) {
            lineCol.line = line;
            lineCol.col = (minChar - lineMap[line]);
        }
    }

    // Return the [line, column] (both 1 based) corresponding to a given position in a given script.
    export function getLineColumnFromPosition(script: TypeScript.Script, position: number): ILineCol {
        var result = { line: -1, col: -1 };
        getSourceLineColFromMap(result, position, script.locationInfo.lineMap);
        if (result.col >= 0) {
            result.col++;   // Make it 1-based
        }
        return result;
    }

    //
    // Return the position (offset) corresponding to a given [line, column] (both 1-based) in a given script.
    //
    export function getPositionFromLineColumn(script: TypeScript.Script, line: number, column: number): number {
        return script.locationInfo.lineMap[line] + (column - 1);
    }
}
