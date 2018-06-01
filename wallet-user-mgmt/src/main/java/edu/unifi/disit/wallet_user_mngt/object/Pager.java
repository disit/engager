/*******************************************************************************
 * Engager
 *    Copyright (C) 2016-2018 DISIT Lab http://www.disit.org - University of Florence
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Affero General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package edu.unifi.disit.wallet_user_mngt.object;

public class Pager {
	private int buttonsToShow = 5;

	private int startPage;

	private int endPage;

	private int totalPages;

	public Pager(int totalPages, int currentPage, int buttonsToShow) {

		setTotalPages(totalPages);

		setButtonsToShow(buttonsToShow);

		int halfPagesToShow = getButtonsToShow() / 2;

		if (totalPages == 0)// mycode
		{
			setStartPage(0);
			setEndPage(0);
		} else if (totalPages <= getButtonsToShow()) {
			setStartPage(1);
			setEndPage(totalPages);

		} else if (currentPage - halfPagesToShow <= 0) {
			setStartPage(1);
			setEndPage(getButtonsToShow());

		} else if (currentPage + halfPagesToShow == totalPages) {
			setStartPage(currentPage - halfPagesToShow);
			setEndPage(totalPages);

		} else if (currentPage + halfPagesToShow > totalPages) {
			setStartPage(totalPages - getButtonsToShow() + 1);
			setEndPage(totalPages);

		} else {
			setStartPage(currentPage - halfPagesToShow);
			setEndPage(currentPage + halfPagesToShow);
		}
	}

	public int getButtonsToShow() {
		return buttonsToShow;
	}

	public void setButtonsToShow(int buttonsToShow) {
		if (buttonsToShow % 2 != 0) {
			this.buttonsToShow = buttonsToShow;
		} else {
			throw new IllegalArgumentException("Must be an odd value!");
		}
	}

	public int getStartPage() {
		return startPage;
	}

	public void setStartPage(int startPage) {
		this.startPage = startPage;
	}

	public int getEndPage() {
		return endPage;
	}

	public void setEndPage(int endPage) {
		this.endPage = endPage;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	@Override
	public String toString() {
		return "Pager [buttonsToShow=" + buttonsToShow + ", startPage=" + startPage + ", endPage=" + endPage + ", totalPages=" + totalPages + "]";
	}
}
