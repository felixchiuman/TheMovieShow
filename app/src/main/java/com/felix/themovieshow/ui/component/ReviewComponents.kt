package com.felix.themovieshow.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.felix.themovieshow.data.api.model.Review
import com.felix.themovieshow.ui.theme.SurfaceDark
import com.felix.themovieshow.ui.theme.TextSecondary

/** Card untuk satu review*/
@Composable
fun ReviewCard(
    review: Review,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(review.author, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            review.authorDetails?.rating?.let { rating ->
                Text("★ $rating", color = TextSecondary, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            review.content,
            color = TextSecondary,
            fontSize = 13.sp,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 18.sp
        )
    }
}

/** Section preview 2-3 review di detail screen, dengan "View All" */
@Composable
fun ReviewPreviewSection(
    reviews: List<Review>,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(top = 12.dp)) {
        SectionHeader(title = "Reviews", onViewAllClick = onSeeAllClick)
        Spacer(Modifier.height(10.dp))
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            reviews.forEach { review ->
                ReviewCard(review = review)
            }
        }
    }
}
