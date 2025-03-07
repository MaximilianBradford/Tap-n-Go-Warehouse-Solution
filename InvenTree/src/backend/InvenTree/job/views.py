from django.shortcuts import render
"""Django views for interacting with Job app."""

from django.http import HttpResponseRedirect
from django.urls import reverse
from django.views.generic import DetailView, ListView

from common.settings import get_global_setting
from InvenTree.views import InvenTreeRoleMixin
from plugin.views import InvenTreePluginViewMixin

from .models import Job, Address, JobItem


class JobIndex(InvenTreeRoleMixin, InvenTreePluginViewMixin, ListView):
    """JobIndex view loads all Job objects."""

    model = Job
    template_name = 'job/job_list.html'
    context_object_name = 'jobs'

    def get_context_data(self, **kwargs):
        """Extend template context."""
        context = super().get_context_data(**kwargs).copy()

        context['jobs'] = Job.objects.all()
        context['job_count'] = Job.objects.count()

        return context


class JobDetail(InvenTreeRoleMixin, InvenTreePluginViewMixin, DetailView):
    """Detailed view of a single Job object."""

    context_object_name = 'job'
    template_name = 'job/job_detail.html'
    queryset = Job.objects.all()
    model = Job

    def get_context_data(self, **kwargs):
        """Extend template context."""
        context = super().get_context_data(**kwargs)

        context['job_items'] = JobItem.objects.filter(job=self.object)
        context['address'] = self.object.address

        return context


class JobItemDetail(InvenTreeRoleMixin, InvenTreePluginViewMixin, DetailView):
    """Detailed view of a single JobItem object."""

    context_object_name = 'job_item'
    template_name = 'job/job_item_detail.html'
    queryset = JobItem.objects.all()
    model = JobItem

    def get_context_data(self, **kwargs):
        """Extend template context."""
        context = super().get_context_data(**kwargs)

        context['job'] = self.object.job
        context['stock_item'] = self.object.stock_item

        return context