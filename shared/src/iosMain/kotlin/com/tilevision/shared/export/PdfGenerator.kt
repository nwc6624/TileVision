package com.tilevision.shared.export

import com.tilevision.shared.domain.ProjectSummary

actual class PdfGenerator {
    
    actual suspend fun generatePdf(summary: ProjectSummary): Result<ByteArray> {
        // TODO: Implement iOS PDF generation using Core Graphics or PDFKit
        // For now, return a simple error
        return Result.failure(Exception("iOS PDF generation not yet implemented"))
    }
}
