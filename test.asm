    processor 6502

;; Zero Page
    seg zero_page

;; Main game
    org $801
    dc.b $0b, $08, $d4, $07, $9e, $32, $30, $36, $31, $00, $00, $00

start subroutine
    jmp main

frame dc.w 0

blue_tab
    dc.b 0
    dc.b 6
    dc.b 14
    dc.b 7
    dc.b 1
    dc.b 7
    dc.b 14
    dc.b 6

get_blue subroutine
    and #7
    tax
    lda blue_tab,x
    rts

irq subroutine
    php
    cld
    pha
    txa
    pha

    lda #$ff
    sta $d019
    inc frame
    bne .skip
    inc frame+1
.skip

    lda frame
    lsr
    lsr
    jsr get_blue
    sta $d021

    pla
    tax
    pla
    plp
    rti

oops subroutine
    sei
    lda #$35
    sta $1
.loop
    inc $d020
    jmp .loop

init subroutine
    ;; disable ints and clear dec mode
    sei
    cld

    ;; Disable the cias / clear pending interrupts
    lda #$7f
    sta $dc0d
    sta $dd0d
    lda $dc0d
    lda $dd0d

    ;; Enable raster interrupts
    lda #1
    sta $d01a
    lda #0
    sta $d012
    lda #$1b
    sta $d011

    ;; Bank out all the shitty rom
    lda #$35
    sta 1
    cli
    rts


main subroutine
    ldx #$ff
    txs
    jsr init

.restart
    ldx #7

.loop
    lda $d012
    ldy blue_tab,x
    dex
    bpl .looper
    ldx #7

.looper
    cmp $d012
    beq .looper

    sty $d020
    cmp #0
    beq .restart
    jmp .loop

    seg vector
    org $fffa
    dc.w oops
    dc.w oops
    dc.w irq


