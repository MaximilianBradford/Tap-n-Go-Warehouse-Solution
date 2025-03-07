"""URL lookup for Job app."""

from django.urls import include, path

from . import views

job_detail_urls = [
    path(
        '<int:pk>/',
        include([
            # Direct to the job detail view
            path('', views.JobDetail.as_view(), name='job-detail')
        ]),
    )
]

job_item_detail_urls = [
    # Direct to the job item detail view
    path('<int:pk>/', views.JobItemDetail.as_view(), name='job-item-detail')
]

job_urls = [
    # Job detail
    path('job/', include(job_detail_urls)),
    # Job items
    path('job-item/', include(job_item_detail_urls)),
    # Default to the job index page
    path('', views.JobIndex.as_view(), name='job-index'),
]

urlpatterns = [
    path('', include(job_urls)),
]