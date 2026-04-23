package com.apktados.ruleta.ui.help

import android.content.Context
import com.apktados.ruleta.R

fun buildHelpHtml(context: Context): String {
    fun s(id: Int) = context.getString(id)

    return """
        <!DOCTYPE html>
        <html lang="${s(R.string.help_lang_code)}">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>${s(R.string.help_page_title)}</title>
            <style>
                body {
                    font-family: Arial, sans-serif;
                    background: #0f172a;
                    color: #ffffff;
                    margin: 0;
                    padding: 20px;
                    line-height: 1.65;
                }

                h1, h2 {
                    color: #facc15;
                    margin-top: 0;
                }

                h1 {
                    font-size: 28px;
                    margin-bottom: 12px;
                }

                h2 {
                    font-size: 20px;
                    margin-bottom: 10px;
                }

                p {
                    margin: 0 0 12px 0;
                }

                .hero-image {
                    width: 100%;
                    border-radius: 12px;
                    margin-bottom: 18px;
                    display: block;
                }

                .intro {
                    background: #172033;
                    border-left: 4px solid #facc15;
                    border-radius: 10px;
                    padding: 14px 16px;
                    margin-bottom: 18px;
                }

                .card {
                    background: #1e293b;
                    border-radius: 12px;
                    padding: 16px;
                    margin-bottom: 16px;
                    box-shadow: 0 2px 6px rgba(0,0,0,0.25);
                }

                ul {
                    padding-left: 22px;
                    margin: 8px 0 0 0;
                }

                li {
                    margin-bottom: 8px;
                }

                strong {
                    color: #fde68a;
                }

                .tip {
                    background: #243449;
                    border: 1px solid #334155;
                    border-radius: 10px;
                    padding: 12px 14px;
                    margin-top: 10px;
                }

                .footer {
                    text-align: center;
                    color: #cbd5e1;
                    font-size: 14px;
                    margin-top: 24px;
                    padding-bottom: 12px;
                }
            </style>
        </head>
        <body>
            <h1>${s(R.string.help_title)}</h1>

            <img
                alt="${s(R.string.help_image_alt)}"
                src="file:///android_res/drawable/ruleta2.png"
                class="hero-image" />

            <div class="intro">
                <p>${s(R.string.help_intro)}</p>
            </div>

            <div class="card">
                <h2>${s(R.string.help_objective_title)}</h2>
                <p>${s(R.string.help_objective_text)}</p>
            </div>

            <div class="card">
                <h2>${s(R.string.help_navigation_title)}</h2>
                <ul>
                    <li><strong>${s(R.string.help_navigation_home_label)}</strong> ${s(R.string.help_navigation_home_text)}</li>
                    <li><strong>${s(R.string.help_navigation_game_label)}</strong> ${s(R.string.help_navigation_game_text)}</li>
                    <li><strong>${s(R.string.help_navigation_history_label)}</strong> ${s(R.string.help_navigation_history_text)}</li>
                    <li><strong>${s(R.string.help_navigation_settings_label)}</strong> ${s(R.string.help_navigation_settings_text)}</li>
                </ul>
            </div>

            <div class="card">
                <h2>${s(R.string.help_how_to_play_title)}</h2>
                <ul>
                    <li>${s(R.string.help_how_to_play_step_1)}</li>
                    <li>${s(R.string.help_how_to_play_step_2)}</li>
                    <li>${s(R.string.help_how_to_play_step_3)}</li>
                    <li>${s(R.string.help_how_to_play_step_4)}</li>
                    <li>${s(R.string.help_how_to_play_step_5)}</li>
                </ul>
            </div>

            <div class="card">
                <h2>${s(R.string.help_bets_title)}</h2>
                <ul>
                    <li><strong>${s(R.string.help_bet_red_black_title)}</strong> ${s(R.string.help_bet_red_black_text)}</li>
                    <li><strong>${s(R.string.help_bet_even_odd_title)}</strong> ${s(R.string.help_bet_even_odd_text)}</li>
                    <li><strong>${s(R.string.help_bet_exact_title)}</strong> ${s(R.string.help_bet_exact_text)}</li>
                </ul>
            </div>

            <div class="card">
                <h2>${s(R.string.help_coins_title)}</h2>
                <p>${s(R.string.help_coins_text)}</p>
                <div class="tip">
                    <strong>${s(R.string.help_tip_label)}</strong> ${s(R.string.help_coins_tip)}
                </div>
            </div>

            <div class="card">
                <h2>${s(R.string.help_history_title)}</h2>
                <p>${s(R.string.help_history_text)}</p>
            </div>

            <div class="card">
                <h2>${s(R.string.help_recommendations_title)}</h2>
                <ul>
                    <li>${s(R.string.help_recommendations_1)}</li>
                    <li>${s(R.string.help_recommendations_2)}</li>
                    <li>${s(R.string.help_recommendations_3)}</li>
                </ul>
            </div>
            
            <div class="card">
                <h2>${s(R.string.help_no_coins_title)}</h2>
                <p>${s(R.string.help_no_coins_text)}</p>
            </div>

            <div class="card">
                <h2>${s(R.string.help_save_games_title)}</h2>
                <p>${s(R.string.help_save_games_text)}</p>
            </div>

            <div class="card">
                <h2>${s(R.string.help_location_title)}</h2>
                <p>${s(R.string.help_location_text)}</p>
            </div>

            <div class="card">
                <h2>${s(R.string.help_notification_title)}</h2>
                <p>${s(R.string.help_notification_text)}</p>
            </div>

            <div class="footer">
                ${s(R.string.help_footer)}
            </div>
        </body>
        </html>
    """.trimIndent()
}